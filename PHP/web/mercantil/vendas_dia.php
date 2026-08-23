<?php
session_start(); ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php'; require_once '../includes/functions.php';
requireAuth(); $title='Vendas do Dia'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$hoje=date('Y-m-d');
$vendas=$db->prepare("SELECT v.*,COUNT(i.id) as itens FROM tb_vendas v LEFT JOIN tb_venda_itens i ON v.id=i.venda_id WHERE DATE(v.data_venda)=? GROUP BY v.id ORDER BY v.id DESC");
$vendas->execute([$hoje]);
$vendas=$vendas->fetchAll(PDO::FETCH_ASSOC);
$totalDia=array_sum(array_column($vendas,'total_venda'));
$qtdVendas=count($vendas);
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-calendar-day"></i> Vendas do Dia — <?=date('d/m/Y')?></h4>
<div class="row g-2 mb-3">
  <div class="col-auto"><div class="card bg-primary text-white p-2"><h5 class="mb-0"><?=$qtdVendas?> vendas</h5></div></div>
  <div class="col-auto"><div class="card bg-success text-white p-2"><h5 class="mb-0">R$ <?=number_format($totalDia,2,',','.')?></h5></div></div>
</div>
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped"><thead class="table-light">
<tr><th>#</th><th>Horário</th><th>Cliente</th><th class="text-end">Total</th><th>Itens</th><th>Obs</th><th></th></tr></thead>
<tbody>
<?php if($vendas): foreach($vendas as $v):?>
<tr><td><?=$v['id']?></td>
  <td><?=date('H:i',strtotime($v['data_venda']))?></td>
  <td><?=htmlspecialchars($v['cliente_id']??'-')?></td>
  <td class="text-end">R$ <?=number_format($v['total_venda'],2,',','.')?></td>
  <td><?=$v['itens']?></td>
  <td><?=htmlspecialchars(substr($v['observacoes']??'',0,40))?></td>
  <td><a href="historico_vendas.php?detalhe=<?=$v['id']?>" class="btn btn-sm btn-outline-info py-0">Detalhes</a></td></tr>
<?php endforeach; else:?>
<tr><td colspan="7" class="text-center text-muted">Nenhuma venda hoje.</td></tr>
<?php endif;?>
</tbody></table></div>
<div class="mt-2"><a href="historico_vendas.php" class="btn btn-outline-primary btn-sm">📊 Histórico de Vendas</a></div>
</div>
<?php include '../includes/footer.php';?>
