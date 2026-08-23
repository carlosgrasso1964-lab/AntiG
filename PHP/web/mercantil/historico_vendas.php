<?php
session_start(); ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php'; require_once '../includes/functions.php';
requireAuth(); $title='Histórico de Vendas'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$dataIni=$_GET['dataIni']??date('01/m/Y');
$dataFim=$_GET['dataFim']??date('t/m/Y');
$detalheId=(int)($_GET['detalhe']??0);

function toSQL($d){ $p=explode('/',$d); return count($p)==3?"{$p[2]}-{$p[1]}-{$p[0]}":''; }

$di=toSQL($dataIni); $df=toSQL($dataFim);

// Vendas do período
$vendas=$db->prepare("SELECT v.*,COUNT(i.id) as itens FROM tb_vendas v LEFT JOIN tb_venda_itens i ON v.id=i.venda_id WHERE DATE(v.data_venda) BETWEEN ? AND ? GROUP BY v.id ORDER BY v.data_venda DESC");
$vendas->execute([$di,$df]);
$vendas=$vendas->fetchAll(PDO::FETCH_ASSOC);

// Totais do período
$totalBruto=0; $totalCusto=0; $totalLucro=0;
foreach($vendas as $v){
    $totalBruto+=$v['total_venda'];
    // Custo dos itens desta venda
    $c=$db->prepare("SELECT COALESCE(SUM(i.qtd * COALESCE(p.preco,0)),0) as custo FROM tb_venda_itens i LEFT JOIN tb_produtos p ON i.produto_id=p.id WHERE i.venda_id=?");
    $c->execute([$v['id']]);
    $custo=(float)$c->fetchColumn();
    $totalCusto+=$custo;
    $totalLucro+=($v['total_venda']-$custo);
}
$margem=$totalBruto>0?($totalLucro/$totalBruto*100):0;

// Detalhe de uma venda específica
$detalhe=[]; $vendaDet=null;
if($detalheId){
    $s=$db->prepare("SELECT * FROM tb_vendas WHERE id=?");
    $s->execute([$detalheId]); $vendaDet=$s->fetch(PDO::FETCH_ASSOC);
    if($vendaDet){
        $detalhe=$db->prepare("SELECT i.*,p.preco as preco_custo FROM tb_venda_itens i LEFT JOIN tb_produtos p ON i.produto_id=p.id WHERE i.venda_id=?");
        $detalhe->execute([$detalheId]); $detalhe=$detalhe->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-clock-history"></i> Histórico de Vendas</h4>
<form method="GET" class="row g-2 mb-3 align-items-end">
  <div class="col-auto"><label class="small">Data Inicial</label><input type="text" name="dataIni" class="form-control form-control-sm" value="<?=$dataIni?>" style="width:120px"></div>
  <div class="col-auto"><label class="small">Data Final</label><input type="text" name="dataFim" class="form-control form-control-sm" value="<?=$dataFim?>" style="width:120px"></div>
  <div class="col-auto"><button type="submit" class="btn btn-primary btn-sm">Filtrar</button>
    <button type="button" class="btn btn-success btn-sm" onclick="exportarFaturamento()">📄 Relatório de Faturamento</button>
    <button type="button" class="btn btn-info btn-sm" onclick="exportarLucro()">💰 Apurar Lucro</button></div>
</form>

<!-- Cards de resumo -->
<div class="row g-2 mb-3">
  <div class="col-md-3"><div class="card bg-primary text-white p-2 text-center"><h6>Faturamento Bruto</h6><h4 class="mb-0">R$ <?=number_format($totalBruto,2,',','.')?></h4></div></div>
  <div class="col-md-3"><div class="card bg-danger text-white p-2 text-center"><h6>Custo Total</h6><h4 class="mb-0">R$ <?=number_format($totalCusto,2,',','.')?></h4></div></div>
  <div class="col-md-3"><div class="card bg-success text-white p-2 text-center"><h6>Lucro Bruto</h6><h4 class="mb-0">R$ <?=number_format($totalLucro,2,',','.')?></h4></div></div>
  <div class="col-md-3"><div class="card bg-warning text-dark p-2 text-center"><h6>Margem</h6><h4 class="mb-0"><?=number_format($margem,1,',','.')?>%</h4></div></div>
</div>

<!-- Tabela de vendas -->
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped" id="tblHistorico">
<thead class="table-light"><tr>
  <th>#</th><th>Data</th><th>Cliente</th><th class="text-end">Faturamento</th><th class="text-end">Custo</th><th class="text-end">Lucro</th><th class="text-end">Margem</th><th>Itens</th><th></th>
</tr></thead>
<tbody>
<?php foreach($vendas as $v):
    $c=$db->prepare("SELECT COALESCE(SUM(i.qtd * COALESCE(p.preco,0)),0) FROM tb_venda_itens i LEFT JOIN tb_produtos p ON i.produto_id=p.id WHERE i.venda_id=?");
    $c->execute([$v['id']]); $custoVenda=(float)$c->fetchColumn();
    $lucroVenda=$v['total_venda']-$custoVenda;
    $margemVenda=$v['total_venda']>0?$lucroVenda/$v['total_venda']*100:0;
?>
<tr>
  <td><?=$v['id']?></td>
  <td><?=date('d/m/Y H:i',strtotime($v['data_venda']))?></td>
  <td><?=htmlspecialchars($v['cliente_id']??'-')?></td>
  <td class="text-end"><?=number_format($v['total_venda'],2,',','.')?></td>
  <td class="text-end"><?=number_format($custoVenda,2,',','.')?></td>
  <td class="text-end <?=$lucroVenda>=0?'text-success':'text-danger'?>"><?=number_format($lucroVenda,2,',','.')?></td>
  <td class="text-end"><?=number_format($margemVenda,1,',','.')?>%</td>
  <td><?=$v['itens']?></td>
  <td><a href="?dataIni=<?=$dataIni?>&dataFim=<?=$dataFim?>&detalhe=<?=$v['id']?>" class="btn btn-sm btn-outline-info py-0">📋</a></td>
</tr>
<?php endforeach; if(!$vendas):?><tr><td colspan="9" class="text-center text-muted">Nenhuma venda no período.</td></tr><?php endif;?>
</tbody></table></div>

<!-- Detalhe da venda -->
<?php if($vendaDet):?>
<div class="card mt-3" id="detalheVenda">
<div class="card-header d-flex justify-content-between align-items-center">
  <strong>Detalhes da Venda #<?=$vendaDet['id']?></strong>
  <span><?=date('d/m/Y H:i',strtotime($vendaDet['data_venda']))?> — <?=htmlspecialchars($vendaDet['cliente_id']??'Consumidor Final')?>
  <button class="btn btn-sm btn-outline-secondary ms-2" onclick="imprimirCupom()">🧾 Cupom</button></span>
</div>
<div class="card-body p-0">
<table class="table table-sm mb-0"><thead><tr><th>Produto</th><th class="text-end">Qtd</th><th class="text-end">Preço</th><th class="text-end">Subtotal</th><th class="text-end">Custo Und</th><th class="text-end">Lucro</th></tr></thead>
<tbody>
<?php $tl=0; foreach($detalhe as $d):
  $l=($d['preco_unitario']-$d['preco_custo'])*$d['qtd']; $tl+=$l;?>
<tr><td><?=htmlspecialchars($d['descricao'])?></td>
  <td class="text-end"><?=number_format($d['qtd'],2,',','.')?></td>
  <td class="text-end"><?=number_format($d['preco_unitario'],2,',','.')?></td>
  <td class="text-end"><?=number_format($d['subtotal'],2,',','.')?></td>
  <td class="text-end"><?=number_format($d['preco_custo']??0,2,',','.')?></td>
  <td class="text-end <?=$l>=0?'text-success':'text-danger'?>"><?=number_format($l,2,',','.')?></td></tr>
<?php endforeach;?>
</tbody>
<tfoot><tr class="fw-bold"><td colspan="5" class="text-end">Lucro Total:</td><td class="text-end <?=$tl>=0?'text-success':'text-danger'?>"><?=number_format($tl,2,',','.')?></td></tr></tfoot>
</table></div></div>
<?php endif;?>
</div>

<script>
function gerarCSV(linhas, nomeArquivo){
    if(!linhas||linhas.length<2){alert('Sem dados para exportar.');return;}
    var csv='\uFEFF'+linhas.join('\n');
    var blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});
    var link=document.createElement('a');link.href=URL.createObjectURL(blob);
    link.download=nomeArquivo+'_'+new Date().toISOString().slice(0,10)+'.csv';link.click();
}
function exportarFaturamento(){
    var rows=[];
    rows.push('Venda;Data;Cliente;Faturamento');
    document.querySelectorAll('#tblHistorico tbody tr').forEach(function(tr){
        var c=tr.cells;if(c.length<9)return;
        rows.push(c[0].textContent.trim()+';'+c[1].textContent.trim()+';'+c[2].textContent.trim()+';'+c[3].textContent.trim().replace('R\$ ',''));
    });
    gerarCSV(rows,'faturamento');
}
function exportarLucro(){
    var rows=[];
    rows.push('Venda;Data;Cliente;Faturamento;Custo;Lucro;Margem');
    document.querySelectorAll('#tblHistorico tbody tr').forEach(function(tr){
        var c=tr.cells;if(c.length<9)return;
        rows.push(c[0].textContent.trim()+';'+c[1].textContent.trim()+';'+c[2].textContent.trim()+';'+c[3].textContent.trim().replace('R\$ ','')+';'+c[4].textContent.trim().replace('R\$ ','')+';'+c[5].textContent.trim().replace('R\$ ','')+';'+c[6].textContent.trim());
    });
    gerarCSV(rows,'apuracao_lucro');
}
function imprimirCupom(){
    var v = <?=json_encode($vendaDet)?>;
    var itens = <?=json_encode($detalhe)?>;
    var total = (v.total_venda||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
    var win = window.open('','_blank','width=320,height=600');
    win.document.write('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Cupom #'+v.id+'</title>');
    win.document.write('<style>body{font-family:Courier,monospace;font-size:12px;width:280px;margin:10px auto}');
    win.document.write('h3{text-align:center;margin:5px 0}.linha{border-top:1px dashed #000;margin:5px 0}.item{margin:2px 0}.total{font-weight:700;font-size:14px}.centro{text-align:center}');
    win.document.write('@media print{body{margin:0}}.footer{text-align:center;font-size:10px;margin-top:10px}</style></head><body>');
    win.document.write('<h3>JFIP WEB</h3>');
    win.document.write('<div class="centro">Venda #'+v.id+'</div>');
    win.document.write('<div class="centro">'+(v.cliente_id||'Consumidor Final')+'</div>');
    win.document.write('<div class="centro">'+new Date(v.data_venda).toLocaleString('pt-BR')+'</div>');
    win.document.write('<div class="linha"></div>');
    itens.forEach(function(i){
        var sub = (i.subtotal||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
        win.document.write('<div class="item"><b>'+i.descricao+'</b></div>');
        win.document.write('<div style="display:flex;justify-content:space-between">');
        win.document.write('<span>'+i.qtd+' x R$ '+(i.preco_unitario||0).toLocaleString('pt-BR',{minimumFractionDigits:2})+'</span>');
        win.document.write('<span>R$ '+sub+'</span></div>');
    });
    win.document.write('<div class="linha"></div>');
    win.document.write('<div class="total" style="display:flex;justify-content:space-between"><span>TOTAL</span><span>R$ '+total+'</span></div>');
    win.document.write('<div class="linha"></div>');
    win.document.write('<div class="footer">Obrigado pela preferência!</div>');
    win.document.write('</body></html>');
    win.document.close();
    win.focus();
    setTimeout(function(){win.print();},500);
}
</script>
<?php include '../includes/footer.php';?>
