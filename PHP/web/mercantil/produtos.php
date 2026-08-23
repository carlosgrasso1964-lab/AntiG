<?php
session_start();
ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title='Produtos'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$msg='';
if(isset($_POST['salvar'])){
  $d=trim($_POST['descricao']);$pc=str_replace(',','.',str_replace('.','',$_POST['preco_compra']??'0'));
  $pv=str_replace(',','.',str_replace('.','',$_POST['preco_venda']??'0'));
  $qt=str_replace(',','.',$_POST['qtd_estoque']??'0');
  $em=str_replace(',','.',$_POST['estoque_min']??'0');
  $ex=str_replace(',','.',$_POST['estoque_max']??'0');
  $pm=str_replace(',','.',str_replace('.','',$_POST['preco_medio']??'0'));
  $cat=(int)($_POST['categoria_id']??0);
  $id=(int)($_POST['id']??0);
  if($d){
    if($id){
      $s=$db->prepare("UPDATE tb_produtos SET descricao=?,preco=?,preco_venda=?,qtd_estoque=?,estoqueMinimo=?,estoqueMaximo=?,precoMedioCusto=?,categoria_id=? WHERE id=?");
      $s->execute([$d,(float)$pc,(float)$pv,(float)$qt,(float)$em,(float)$ex,(float)$pm,$cat,$id]);
      $msg='<div class="alert alert-success">Produto atualizado!</div>';
    } else {
      $s=$db->prepare("INSERT INTO tb_produtos (descricao,preco,preco_venda,qtd_estoque,estoqueMinimo,estoqueMaximo,precoMedioCusto,categoria_id) VALUES (?,?,?,?,?,?,?,?)");
      $s->execute([$d,(float)$pc,(float)$pv,(float)$qt,(float)$em,(float)$ex,(float)$pm,$cat]);
      $msg='<div class="alert alert-success">Produto criado!</div>';
    }
  }
}
if(isset($_POST['excluir'])){
  $db->prepare("DELETE FROM tb_produtos WHERE id=?")->execute([(int)($_POST['id']??0)]);
  $msg='<div class="alert alert-success">Produto excluído!</div>';
}
$catList=$db->query("SELECT id,nome FROM tb_categorias ORDER BY nome")->fetchAll(PDO::FETCH_ASSOC);
$rows=$db->query("SELECT p.*,c.nome as cat_nome FROM tb_produtos p LEFT JOIN tb_categorias c ON p.categoria_id=c.id ORDER BY p.descricao")->fetchAll(PDO::FETCH_ASSOC);
?>
<div class="container-fluid mt-3">
<h4>Produtos</h4><?=$msg?>
<form method="POST" class="row g-2 mb-3 p-2 bg-light border rounded align-items-end">
  <input type="hidden" name="id" id="editId" value="0">
  <div class="col-12"><label class="small">Descrição</label><input type="text" name="descricao" id="editDesc" class="form-control form-control-sm" required></div>
  <div class="col-2"><label class="small">Categoria</label>
    <select name="categoria_id" id="editCat" class="form-select form-select-sm"><option value="0">Sem categoria</option>
    <?php foreach($catList as $c):?><option value="<?=$c['id']?>"><?=htmlspecialchars($c['nome'])?></option><?php endforeach;?></select></div>
  <div class="col-2"><label class="small">Preço Compra</label><input type="text" name="preco_compra" id="editPC" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-2"><label class="small">Preço Venda</label><input type="text" name="preco_venda" id="editPV" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-2"><label class="small">Preço Médio Custo</label><input type="text" name="preco_medio" id="editPM" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-1"><label class="small">Estoque</label><input type="text" name="qtd_estoque" id="editQT" class="form-control form-control-sm" value="0"></div>
  <div class="col-1"><label class="small">Mínimo</label><input type="text" name="estoque_min" id="editEMin" class="form-control form-control-sm" value="0"></div>
  <div class="col-1"><label class="small">Máximo</label><input type="text" name="estoque_max" id="editEMax" class="form-control form-control-sm" value="0"></div>
  <div class="col-auto"><button type="submit" name="salvar" class="btn btn-success btn-sm">Salvar</button>
    <button type="submit" name="excluir" class="btn btn-danger btn-sm" onclick="return confirm('Excluir produto?')">Excluir</button>
    <button type="button" class="btn btn-secondary btn-sm" onclick="limpar()">Limpar</button></div>
</form>
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped" id="tblProdutos">
<thead class="table-light"><tr><th>#</th><th>Descrição</th><th>Categoria</th><th class="text-end">Preço</th><th class="text-end">Venda</th><th class="text-end">Estoque</th><th class="text-end">Mín</th><th class="text-end">Máx</th><th>Ações</th></tr></thead>
<tbody><?php foreach($rows as $r):
$estClass=$r['qtd_estoque']<=0?'text-danger fw-bold':($r['qtd_estoque']<=$r['estoqueMinimo']?'text-warning':'');
?><tr>
<td><?=$r['id']?></td>
<td><?=htmlspecialchars($r['descricao'])?></td>
<td><?=htmlspecialchars($r['cat_nome']??'')?></td>
<td class="text-end"><?=number_format($r['preco'],2,',','.')?></td>
<td class="text-end"><?=number_format($r['preco_venda'],2,',','.')?></td>
<td class="text-end <?=$estClass?>"><?=number_format($r['qtd_estoque'],0,',','.')?></td>
<td class="text-end"><?=number_format($r['estoqueMinimo'],0,',','.')?></td>
<td class="text-end"><?=number_format($r['estoqueMaximo'],0,',','.')?></td>
<td><button class="btn btn-outline-primary btn-sm py-0" onclick="editar(<?=$r['id']?>,'<?=htmlspecialchars($r['descricao'],ENT_QUOTES)?>',<?=$r['categoria_id']?>,<?=$r['preco']?>,<?=$r['preco_venda']?>,<?=$r['qtd_estoque']?>,<?=$r['estoqueMinimo']?>,<?=$r['estoqueMaximo']?>,<?=$r['precoMedioCusto']?>)">✎</button></td></tr>
<?php endforeach;?></tbody></table></div></div>
<script>
function editar(id,desc,cat,pc,pv,qt,emin,emax,pm){
  document.getElementById('editId').value=id;
  document.getElementById('editDesc').value=desc;
  document.getElementById('editCat').value=cat;
  document.getElementById('editPC').value=pc.toLocaleString('pt-BR',{minimumFractionDigits:2});
  document.getElementById('editPV').value=pv.toLocaleString('pt-BR',{minimumFractionDigits:2});
  document.getElementById('editQT').value=Number.isInteger(qt)?qt.toLocaleString('pt-BR'):qt.toLocaleString('pt-BR',{minimumFractionDigits:1});
  document.getElementById('editEMin').value=Number.isInteger(emin)?emin.toLocaleString('pt-BR'):emin.toLocaleString('pt-BR',{minimumFractionDigits:1});
  document.getElementById('editEMax').value=Number.isInteger(emax)?emax.toLocaleString('pt-BR'):emax.toLocaleString('pt-BR',{minimumFractionDigits:1});
  document.getElementById('editPM').value=pm.toLocaleString('pt-BR',{minimumFractionDigits:2});
  window.scrollTo({top:0,behavior:'smooth'});
}
function limpar(){
  ['editId','editDesc','editPC','editPV','editQT','editEMin','editEMax','editPM'].forEach(function(f){document.getElementById(f).value='';});
  document.getElementById('editCat').value=0;
  document.getElementById('editPC').value='0,00';
  document.getElementById('editPV').value='0,00';
  document.getElementById('editPM').value='0,00';
  document.getElementById('editQT').value='0';
  document.getElementById('editEMin').value='0';
  document.getElementById('editEMax').value='0';
}
</script>
<?php include '../includes/footer.php';?>
