<?php
session_start(); ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php'; require_once '../includes/functions.php';
requireAuth(); $title='Histórico de Compras'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$dataIni=$_GET['dataIni']??date('01/m/Y');
$dataFim=$_GET['dataFim']??date('t/m/Y');
$detalheId=(int)($_GET['detalhe']??0);
function toSQL($d){$p=explode('/',$d);return count($p)==3?"{$p[2]}-{$p[1]}-{$p[0]}":'';}
$di=toSQL($dataIni);$df=toSQL($dataFim);

$compras=$db->prepare("SELECT c.*,COUNT(i.id) as itens FROM tb_compras c LEFT JOIN tb_compra_itens i ON c.id=i.compra_id WHERE DATE(c.data_compra) BETWEEN ? AND ? GROUP BY c.id ORDER BY c.data_compra DESC");
$compras->execute([$di,$df]);$compras=$compras->fetchAll(PDO::FETCH_ASSOC);
$totalPeriodo=array_sum(array_column($compras,'total_compra'));

$detalhe=[];$compraDet=null;
if($detalheId){
    $s=$db->prepare("SELECT * FROM tb_compras WHERE id=?");$s->execute([$detalheId]);$compraDet=$s->fetch(PDO::FETCH_ASSOC);
    if($compraDet){$stmt=$db->prepare("SELECT * FROM tb_compra_itens WHERE compra_id=?");$stmt->execute([$detalheId]);$detalhe=$stmt->fetchAll(PDO::FETCH_ASSOC);}
}
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-clock-history"></i> Histórico de Compras</h4>
<form method="GET" class="row g-2 mb-3 align-items-end">
  <div class="col-auto"><label class="small">Data Inicial</label><input type="text" name="dataIni" class="form-control form-control-sm" value="<?=$dataIni?>" style="width:120px"></div>
  <div class="col-auto"><label class="small">Data Final</label><input type="text" name="dataFim" class="form-control form-control-sm" value="<?=$dataFim?>" style="width:120px"></div>
  <div class="col-auto"><button type="submit" class="btn btn-primary btn-sm">Filtrar</button>
    <button type="button" class="btn btn-info btn-sm" onclick="exportarCSV()">📄 Exportar</button></div>
</form>
<div class="row g-2 mb-3"><div class="col-auto"><div class="card bg-primary text-white p-2"><h5 class="mb-0">Total: R$ <?=number_format($totalPeriodo,2,',','.')?></h5></div></div></div>

<div class="table-responsive">
<table class="table table-sm table-bordered table-striped" id="tblHistorico">
<thead class="table-light"><tr><th>#</th><th>Data</th><th>Fornecedor</th><th class="text-end">Total</th><th>Itens</th><th>Obs</th><th></th></tr></thead>
<tbody>
<?php foreach($compras as $c):?>
<tr><td><?=$c['id']?></td><td><?=date('d/m/Y H:i',strtotime($c['data_compra']))?></td>
  <td><?=htmlspecialchars($c['fornecedor_id']??'-')?></td>
  <td class="text-end"><?=number_format($c['total_compra'],2,',','.')?></td>
  <td><?=$c['itens']?></td><td><?=htmlspecialchars(substr($c['observacoes']??'',0,40))?></td>
  <td><a href="?dataIni=<?=$dataIni?>&dataFim=<?=$dataFim?>&detalhe=<?=$c['id']?>" class="btn btn-sm btn-outline-info py-0">📋</a></td></tr>
<?php endforeach; if(!$compras):?><tr><td colspan="7" class="text-center text-muted">Nenhuma compra no período.</td></tr><?php endif;?>
</tbody></table></div>

<?php if($compraDet):?>
<div class="card mt-3" id="detalheCompra">
<div class="card-header d-flex justify-content-between align-items-center">
  <strong>Detalhes da Compra #<?=$compraDet['id']?></strong>
  <span><?=date('d/m/Y H:i',strtotime($compraDet['data_compra']))?> — <?=htmlspecialchars($compraDet['fornecedor_id']??'Fornecedor')?>
  <button class="btn btn-sm btn-outline-secondary ms-2" onclick="imprimirOC()">📄 OC</button></span>
</div>
<div class="card-body p-0">
<table class="table table-sm mb-0"><thead><tr><th>Produto</th><th class="text-end">Qtd</th><th class="text-end">Preço Und</th><th class="text-end">Subtotal</th></tr></thead>
<tbody>
<?php foreach($detalhe as $d):?>
<tr><td><?=htmlspecialchars($d['descricao'])?></td>
  <td class="text-end"><?=number_format($d['qtd'],2,',','.')?></td>
  <td class="text-end"><?=number_format($d['preco_unitario'],2,',','.')?></td>
  <td class="text-end"><?=number_format($d['subtotal'],2,',','.')?></td></tr>
<?php endforeach;?>
</tbody>
<tfoot><tr class="fw-bold"><td colspan="3" class="text-end">TOTAL:</td><td class="text-end"><?=number_format($compraDet['total_compra'],2,',','.')?></td></tr></tfoot>
</table></div></div>
<?php endif;?>
</div>

<script>
function exportarCSV(){
    var rows=['Compra;Data;Fornecedor;Total'];
    document.querySelectorAll('#tblHistorico tbody tr').forEach(function(tr){
        var c=tr.cells;if(c.length<7)return;
        rows.push(c[0].textContent.trim()+';'+c[1].textContent.trim()+';'+c[2].textContent.trim()+';'+c[3].textContent.trim().replace('R$ ',''));
    });
    var csv='\uFEFF'+rows.join('\n');var blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});
    var link=document.createElement('a');link.href=URL.createObjectURL(blob);
    link.download='compras_<?=$dataIni?>_<?=$dataFim?>.csv';link.click();
}
function imprimirOC(){
    var comp = <?=json_encode($compraDet)?>;
    var itens = <?=json_encode($detalhe)?>;
    var total = (comp.total_compra||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
    var win=window.open('','_blank','width=700,height=600');
    win.document.write('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>OC #'+comp.id+'</title>');
    win.document.write('<style>body{font-family:Arial,sans-serif;font-size:13px;width:650px;margin:20px auto}');
    win.document.write('h2{text-align:center;margin:5px 0}.header{display:flex;justify-content:space-between;margin:10px 0}');
    win.document.write('table{width:100%;border-collapse:collapse}th,td{border:1px solid #000;padding:5px;text-align:left}');
    win.document.write('th{background:#eee}.total{font-weight:700;font-size:15px}.footer{text-align:center;margin-top:20px;font-size:11px}');
    win.document.write('.obs{margin:10px 0;padding:10px;border:1px dashed #999}');
    win.document.write('@media print{body{margin:10px}button{display:none}}</style></head><body>');
    win.document.write('<h2>ORDEM DE COMPRA</h2>');
    win.document.write('<div class="header"><div><strong>OC Nº:</strong> '+comp.id+'<br><strong>Data:</strong> '+new Date(comp.data_compra).toLocaleString('pt-BR')+'</div>');
    win.document.write('<div><strong>Fornecedor:</strong> '+(comp.fornecedor_id||'N/I')+'</div></div>');
    win.document.write('<table><thead><tr><th>Item</th><th>Descrição</th><th style="text-align:right">Qtd</th><th style="text-align:right">Preço Und.</th><th style="text-align:right">Total</th></tr></thead><tbody>');
    itens.forEach(function(i,idx){
        win.document.write('<tr><td>'+(idx+1)+'</td><td>'+i.descricao+'</td><td style="text-align:right">'+i.qtd.toLocaleString('pt-BR',{minimumFractionDigits:2})+'</td><td style="text-align:right">R$ '+(i.preco_unitario||0).toLocaleString('pt-BR',{minimumFractionDigits:2})+'</td><td style="text-align:right">R$ '+(i.subtotal||0).toLocaleString('pt-BR',{minimumFractionDigits:2})+'</td></tr>');
    });
    win.document.write('</tbody></table>');
    win.document.write('<div style="text-align:right;margin-top:10px" class="total">TOTAL: R$ '+total+'</div>');
    if(comp.observacoes) win.document.write('<div class="obs"><strong>Observações:</strong><br>'+comp.observacoes+'</div>');
    win.document.write('<div class="footer">Documento emitido em '+new Date().toLocaleString('pt-BR')+' - JFIP WEB</div>');
    win.document.write('</body></html>');
    win.document.close();win.focus();
    setTimeout(function(){win.print();},500);
}
</script>
<?php include '../includes/footer.php';?>
