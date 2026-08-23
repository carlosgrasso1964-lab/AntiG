<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= $title ?? 'Sistema Financeiro' ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/select2-bootstrap-5-theme@1.3.0/dist/select2-bootstrap-5-theme.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.datatables.net/2.2.2/css/dataTables.bootstrap5.min.css">
    <?php
    $scriptDir = dirname($_SERVER['SCRIPT_NAME']);
    $subdirs = ['cadastros', 'movimentacao', 'consultas', 'relatorios', 'auth', 'utils', 'mercantil', 'manutencao', 'utilitarios'];
    if (in_array(basename($scriptDir), $subdirs)) {
        $baseUrl = dirname($scriptDir) . '/';
    } else {
        $baseUrl = $scriptDir . '/';
    }
    ?>
    <link href="<?= $baseUrl ?>assets/css/style.css?v=<?= @filemtime(__DIR__ . '/../assets/css/style.css') ?>" rel="stylesheet">
    <script>
    // Tema claro/escuro — aplica antes do render para evitar flash (FOUC)
    (function () {
        var t = localStorage.getItem('jfipTheme');
        if (t !== 'light' && t !== 'dark') {
            t = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
        document.documentElement.setAttribute('data-bs-theme', t);
    })();
    </script>
</head>
<body>
    <?php if (isAuthenticated()): ?>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="<?= $baseUrl ?>dashboard.php">
                <i class="bi bi-calculator"></i> jFIP Web
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-files"></i> Cadastros
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>cadastros/plano_contas.php">Plano de Contas</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>cadastros/recursos.php">Recursos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>cadastros/clifor.php">Clientes / Fornecedores</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>cadastros/usuarios.php"><i class="bi bi-person-lock"></i> Usuários</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-arrow-left-right"></i> Movimentação
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>movimentacao/lancamentos.php">Lançamentos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>movimentacao/transferencias.php">Transferências</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>movimentacao/parcelamentos.php">Parcelamentos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>movimentacao/parcelamento_cartoes.php"><i class="bi bi-credit-card"></i> Parcelamento de Cartões</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-search"></i> Consultas
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/saldos.php">Saldos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/fluxo_caixa.php">Fluxo de Caixa</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/receitas_despesas.php">Receitas x Despesas</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/balanco.php">Balanço</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/recursos.php">Recursos</a></li>
                            <li class="dropdown-submenu">
                                <a class="dropdown-item dropdown-toggle" href="<?= $baseUrl ?>consultas/favorecidos.php">Favorecidos</a>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/fornecedores.php">Fornecedores</a></li>
                                    <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/clientes.php">Clientes</a></li>
                                </ul>
                            </li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/classificacao.php">Classificação</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/referencia_cruzada.php">Referência Cruzada</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/inflacao_pessoal.php">Inflação Pessoal</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/painel_diario.php">Painel Diário</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/auditoria.php">Auditoria</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-kanban"></i> Projetos
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>consultas/projetos.php">Projetos e Etapas</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-shop"></i> Mercantil
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/index.php">Painel Mercantil</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/pdv.php">🧾 PDV - Ponto de Venda</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/vendas_dia.php">📅 Vendas do Dia</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/historico_vendas.php">📊 Histórico de Vendas</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/pdc.php">🚚 PDC - Ponto de Compra</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/compras_dia.php">📅 Compras do Dia</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/historico_compras.php">📊 Histórico de Compras</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/categorias.php">Categorias</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/produtos.php">Produtos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>mercantil/precificacao.php">🧮 Precificação</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-file-pdf"></i> Relatórios
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>relatorios/plano_contas.php">Plano de Contas</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>relatorios/recursos.php">Recursos</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>relatorios/favorecidos.php">Favorecidos</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-tools"></i> Manutenção
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>manutencao/backup.php"><i class="bi bi-database-gear"></i> Backup / Restore</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-grid"></i> Utilitários
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>utilitarios/calendario.php"><i class="bi bi-calendar3"></i> Calendário To-Do</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>utilitarios/bloco_notas.php"><i class="bi bi-journal-text"></i> Bloco de Notas</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>utilitarios/calculadora.php"><i class="bi bi-calculator"></i> Calculadora</a></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>utilitarios/aniversariantes.php"><i class="bi bi-balloon-heart"></i> Aniversariantes</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>utilitarios/abrir_pdf.php"><i class="bi bi-file-earmark-pdf"></i> Abrir PDF</a></li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link" href="#" role="button" id="btnTema" title="Alternar tema claro/escuro"
                           onclick="alternarTema(); return false;">
                            <i class="bi bi-moon-stars"></i>
                        </a>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle"></i> <?= htmlspecialchars($_SESSION['usuario_nome']) ?>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="<?= $baseUrl ?>auth/logout.php"><i class="bi bi-box-arrow-right"></i> Sair</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    <?php endif; ?>
    <div class="container-fluid mt-3">
        <?php if ($flash = getFlash()): ?>
            <div class="alert alert-<?= $flash['type'] ?> alert-dismissible fade show">
                <?= $flash['message'] ?>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <?php endif; ?>
    </div>

</html>