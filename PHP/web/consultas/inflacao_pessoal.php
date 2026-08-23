<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title = 'Inflação Pessoal';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();

$inflacao = 0.0;
$mensagem = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $inflacao = (float)($_POST['percentual'] ?? 0);
    $mensagem = "Ajuste de inflação aplicado: " . number_format($inflacao * 100, 2) . "%";
}
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-percentage"></i> Inflação Pessoal</h4>
</div>

<div class="card">
    <div class="card-body">
        <form method="POST" class="row g-3">
            <div class="col-md-4">
                <label for="percentual" class="form-label">Percentual de Inflação</label>
                <input type="number" step="0.01" min="0" class="form-control" id="percentual" name="percentual" value="<?= htmlspecialchars($inflacao) ?>">
            </div>
            <div class="col-md-8">
                <button type="submit" class="btn btn-primary">Aplicar</button>
                <?php if ($mensagem): ?>
                    <div class="alert alert-info"><?= $mensagem ?></div>
                <?php endif; ?>
            </div>
        </form>
    </div>
</div>

<div class="card mt-3">
    <div class="card-header"><i class="bi bi-calculator"></i> Exemplo de Cálculo</div>
    <div class="card-body">
        <p>Se o saldo for R$ 1.000,00 e a inflação for 5,00%:</p>
        <ul class="list-group">
            <li class="list-group-item">Saldo: R$ 1.000,00</li>
            <li class="list-group-item">Inflação: 5,00%</li>
            <li class="list-group-item">Valor ajustado: R$ 1.050,00</li>
        </ul>
    </div>
</div>

<?php include '../includes/footer.php'; ?>