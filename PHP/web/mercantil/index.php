<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title = 'Mercantil';
include '../includes/header.php';
$db = Database::getInstance()->getConnection();
// Contagens
$cat = $db->query("SELECT COUNT(*) FROM tb_categorias")->fetchColumn();
$pro = $db->query("SELECT COUNT(*) FROM tb_produtos")->fetchColumn();
$estBaixo = $db->query("SELECT COUNT(*) FROM tb_produtos WHERE qtd_estoque <= estoqueMinimo AND qtd_estoque > 0")->fetchColumn();
$estZerado = $db->query("SELECT COUNT(*) FROM tb_produtos WHERE qtd_estoque <= 0")->fetchColumn();
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-shop"></i> Mercantil</h4>
<div class="row g-3 mt-2">
  <div class="col-md-3"><a href="categorias.php" class="text-decoration-none">
    <div class="card border-primary h-100"><div class="card-body text-center">
      <h1 class="text-primary"><?= $cat ?></h1>
      <h6>Categorias</h6>
      <small class="text-muted">Gerenciar categorias de produtos</small>
    </div></div></a>
  </div>
  <div class="col-md-3"><a href="produtos.php" class="text-decoration-none">
    <div class="card border-success h-100"><div class="card-body text-center">
      <h1 class="text-success"><?= $pro ?></h1>
      <h6>Produtos</h6>
      <small class="text-muted">Cadastro de produtos e serviços</small>
    </div></div></a>
  </div>
  <div class="col-md-3">
    <div class="card border-warning h-100"><div class="card-body text-center">
      <h1 class="text-warning"><?= $estBaixo ?></h1>
      <h6>Estoque Baixo</h6>
      <small class="text-muted">Produtos próximos do mínimo</small>
    </div></div>
  </div>
  <div class="col-md-3">
    <div class="card border-danger h-100"><div class="card-body text-center">
      <h1 class="text-danger"><?= $estZerado ?></h1>
      <h6>Estoque Zerado</h6>
      <small class="text-muted">Produtos sem estoque</small>
    </div></div>
  </div>
</div>
<div class="row g-3 mt-4">
  <div class="col-md-6">
    <div class="card"><div class="card-header"><strong>Produtos com Estoque Baixo</strong></div>
      <div class="card-body p-0">
        <table class="table table-sm mb-0">
          <thead><tr><th>Produto</th><th>Qtd</th><th>Mínimo</th></tr></thead>
          <tbody>
            <?php $r=$db->query("SELECT descricao,qtd_estoque,estoqueMinimo FROM tb_produtos WHERE qtd_estoque <= estoqueMinimo AND qtd_estoque > 0 ORDER BY qtd_estoque LIMIT 10");
            if($r->rowCount()): foreach($r as $p): ?>
            <tr><td><?=htmlspecialchars($p['descricao'])?></td><td class="text-warning"><?=$p['qtd_estoque']?></td><td><?=$p['estoqueMinimo']?></td></tr>
            <?php endforeach; else: ?><tr><td colspan="3" class="text-muted text-center">Nenhum</td></tr><?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <div class="col-md-6">
    <div class="card"><div class="card-header"><strong>Produtos sem Estoque</strong></div>
      <div class="card-body p-0">
        <table class="table table-sm mb-0">
          <thead><tr><th>Produto</th><th>Estoque</th></tr></thead>
          <tbody>
            <?php $r=$db->query("SELECT descricao,qtd_estoque FROM tb_produtos WHERE qtd_estoque <= 0 ORDER BY descricao LIMIT 10");
            if($r->rowCount()): foreach($r as $p): ?>
            <tr><td><?=htmlspecialchars($p['descricao'])?></td><td class="text-danger"><?=$p['qtd_estoque']?></td></tr>
            <?php endforeach; else: ?><tr><td colspan="2" class="text-muted text-center">Nenhum</td></tr><?php endif; ?>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</div>
<?php include '../includes/footer.php'; ?>
