<?php
session_start(); ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php'; require_once '../includes/functions.php';
requireAuth(); $title='Compras do Dia'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$hoje=date('Y-m-d');
$compras=$db->prepare("SELECT c.*,COUNT(i.id) as itens FROM tb_compras c LEFT JOIN tb_compra_itens i ON c.id=i.compra_id WHERE DATE(c.data_compra)=? GROUP BY c.id ORDER BY c.id DESC");
$compras->execute([$hoje]); $compras=$compras->fetchAll(PDO::FETCH_ASSOC);
$totalDia=array_sum(array_column($compras,'total_compra'));
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-calendar-day"></i> Compras do Dia — <?=date('d/m/Y')?></h4>
<div class="row g-2 mb-3">
  <div class="col-auto"><div class="card bg-primary text-white p-2"><h5 class="mb-0"><?=count($compras)?> compras</h5></div></div>
  <div class="col-auto"><div class="card bg-info text-white p-2"><h5 class="mb-0">R$ <?=number_format($totalDia,2,',','.')?></h5></div></div>
</div>
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped"><thead class="table-light">
<tr><th>#</th><th>Horário</th><th>Fornecedor</th><th class="text-end">Total</th><th>Itens</th><th>Obs</th><th></th></tr></thead>
<tbody>
<?php if($compras): foreach($compras as $c):?>
<tr><td><?=$c['id']?></td><td><?=date('H:i',strtotime($c['data_compra']))?></td>
  <td><?=htmlspecialchars($c['fornecedor_id']??'-')?></td>
  <td class="text-end">R$ <?=number_format($c['total_compra'],2,',','.')?></td>
  <td><?=$c['itens']?></td>
  <td><?=htmlspecialchars(substr($c['observacoes']??'',0,40))?></td>
  <td><a href="historico_compras.php?detalhe=<?=$c['id']?>" class="btn btn-sm btn-outline-info py-0">Detalhes</a></td></tr>
<?php endforeach; else:?><tr><td colspan="7" class="text-center text-muted">Nenhuma compra hoje.</td></tr><?php endif;?>
</tbody></table></div>
<div class="mt-2"><a href="historico_compras.php" class="btn btn-outline-primary btn-sm">📊 Histórico de Compras</a></div></div>
<?php include '../includes/footer.php';?>
