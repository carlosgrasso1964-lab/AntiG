<?php
session_start();
ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title='Categorias'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$msg='';
if(isset($_POST['salvar'])){
  $n=trim($_POST['nome']);$m=str_replace(',','.',$_POST['multiplicador']??'1.0');
  $id=(int)($_POST['id']??0);
  if($n){
    if($id){
      $db->prepare("UPDATE tb_categorias SET nome=?,multiplicador=? WHERE id=?")->execute([$n,(float)$m,$id]);
      $msg='<div class="alert alert-success">Categoria atualizada!</div>';
    } else {
      $db->prepare("INSERT INTO tb_categorias (nome,multiplicador) VALUES (?,?)")->execute([$n,(float)$m]);
      $msg='<div class="alert alert-success">Categoria criada!</div>';
    }
  }
}
if(isset($_POST['excluir'])){
  $db->prepare("DELETE FROM tb_categorias WHERE id=?")->execute([(int)($_POST['id']??0)]);
  $msg='<div class="alert alert-success">Categoria excluída!</div>';
}
$rows=$db->query("SELECT * FROM tb_categorias ORDER BY nome")->fetchAll(PDO::FETCH_ASSOC);
?>
<div class="container-fluid mt-3">
<h4>Categorias</h4><?=$msg?>
<form method="POST" class="row g-2 mb-3 p-2 bg-light border rounded align-items-end">
  <input type="hidden" name="id" id="editId" value="0">
  <div class="col-auto"><label class="small">Nome</label><input type="text" name="nome" id="editNome" class="form-control form-control-sm" style="width:300px" required></div>
  <div class="col-auto"><label class="small">Multiplicador</label><input type="text" name="multiplicador" id="editMult" class="form-control form-control-sm" value="1,0" style="width:80px"></div>
  <div class="col-auto"><button type="submit" name="salvar" class="btn btn-success btn-sm">Salvar</button>
    <button type="submit" name="excluir" class="btn btn-danger btn-sm" onclick="return confirm('Excluir categoria?')">Excluir</button>
    <button type="button" class="btn btn-secondary btn-sm" onclick="limpar()">Limpar</button></div>
</form>
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped"><thead class="table-light"><tr><th>#</th><th>Nome</th><th>Multiplicador</th><th>Ações</th></tr></thead>
<tbody><?php foreach($rows as $r):?>
<tr><td><?=$r['id']?></td><td><?=htmlspecialchars($r['nome'])?></td><td><?=number_format($r['multiplicador'],2,',','.')?></td>
  <td><button class="btn btn-outline-primary btn-sm py-0" onclick="editar(<?=$r['id']?>,'<?=htmlspecialchars($r['nome'],ENT_QUOTES)?>',<?=$r['multiplicador']?>)">✎</button></td></tr>
<?php endforeach;?></tbody></table></div></div>
<script>
function editar(id,nome,mult){
  document.getElementById('editId').value=id;
  document.getElementById('editNome').value=nome;
  document.getElementById('editMult').value=mult.toLocaleString('pt-BR',{minimumFractionDigits:2});
}
function limpar(){
  document.getElementById('editId').value=0;
  document.getElementById('editNome').value='';
  document.getElementById('editMult').value='1,0';
}
</script>
<?php include '../includes/footer.php';?>
