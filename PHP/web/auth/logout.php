<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';

if (isset($_SESSION['usuario_nome'])) {
    registrarLog($_SESSION['usuario_nome'], 'Logout');
}

session_destroy();
header('Location: ../index.php');
exit;
