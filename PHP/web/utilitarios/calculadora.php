<?php
session_start();
require_once '../includes/functions.php';
requireAuth();

$title = 'Calculadora';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-calculator"></i> Calculadora</h4>
</div>

<div class="row justify-content-center">
    <div class="col-md-4 col-lg-3">
        <div class="card shadow">
            <div class="card-body">
                <!-- Display -->
                <input type="text" id="calcDisplay" class="form-control form-control-lg text-end mb-3"
                       style="font-family: Consolas, monospace; font-size: 1.6rem;" value="0" readonly>

                <!-- Teclado -->
                <div class="row g-1">
                    <div class="col-3"><button class="btn btn-outline-secondary w-100" onclick="calc('C')">C</button></div>
                    <div class="col-3"><button class="btn btn-outline-secondary w-100" onclick="calc('(')">(</button></div>
                    <div class="col-3"><button class="btn btn-outline-secondary w-100" onclick="calc(')')">)</button></div>
                    <div class="col-3"><button class="btn btn-outline-danger w-100" onclick="calc('⌫')">⌫</button></div>

                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('7')">7</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('8')">8</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('9')">9</button></div>
                    <div class="col-3"><button class="btn btn-outline-warning w-100" onclick="calc('/')">÷</button></div>

                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('4')">4</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('5')">5</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('6')">6</button></div>
                    <div class="col-3"><button class="btn btn-outline-warning w-100" onclick="calc('*')">×</button></div>

                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('1')">1</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('2')">2</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('3')">3</button></div>
                    <div class="col-3"><button class="btn btn-outline-warning w-100" onclick="calc('-')">−</button></div>

                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('0')">0</button></div>
                    <div class="col-3"><button class="btn btn-outline-primary w-100" onclick="calc('.')">.</button></div>
                    <div class="col-6"><button class="btn btn-outline-warning w-100" onclick="calc('+')">+</button></div>

                    <div class="col-12 mt-1">
                        <button class="btn btn-success w-100 btn-lg" onclick="calcular()">=</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Calculadora simples e segura: avalia apenas expressões aritméticas básicas
let expr = '';

function calc(v) {
    const disp = document.getElementById('calcDisplay');
    if (v === 'C') { expr = ''; disp.value = '0'; return; }
    if (v === '⌫') { expr = expr.slice(0, -1); disp.value = expr || '0'; return; }
    expr += v;
    disp.value = expr;
}

function calcular() {
    const disp = document.getElementById('calcDisplay');
    if (!expr) return;
    // Sanitiza: só dígitos, operadores, parênteses, ponto e vírgula
    if (!/^[0-9+\-*/().\s]+$/.test(expr)) {
        disp.value = 'Erro';
        expr = '';
        return;
    }
    try {
        // Converte ÷ e × para JS
        const resultado = Function('"use strict"; return (' + expr.replace(/÷/g, '/').replace(/×/g, '*') + ')')();
        if (typeof resultado !== 'number' || !isFinite(resultado)) {
            disp.value = 'Erro';
            expr = '';
            return;
        }
        disp.value = parseFloat(resultado.toPrecision(12)).toString().replace('.', ',');
        expr = disp.value.replace(',', '.');
    } catch (e) {
        disp.value = 'Erro';
        expr = '';
    }
}

// Suporte ao teclado
document.addEventListener('keydown', function (e) {
    const teclas = '0123456789+-*/().';
    if (teclas.includes(e.key)) { calc(e.key); }
    else if (e.key === 'Enter') { calcular(); }
    else if (e.key === 'Backspace') { calc('⌫'); }
    else if (e.key === 'Escape') { calc('C'); }
});
</script>

<?php include '../includes/footer.php'; ?>
