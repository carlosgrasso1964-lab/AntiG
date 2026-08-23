<?php
session_start();
ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title='PDV - Ponto de Venda';
include '../includes/header.php';
$db=Database::getInstance()->getConnection();

// === Finalizar venda ===
$msg='';
if(isset($_POST['finalizar'])){
    $cliente=trim($_POST['cliente']??'');
    $obs=$_POST['obs']??'';
    $itens=json_decode($_POST['itens_json']??'[]',true);
    $total=(float)str_replace(',','.',$_POST['total']??'0');
    if(count($itens)==0){$msg='<div class="alert alert-danger">Adicione itens à venda.</div>';}
    else {
        try{
            $db->beginTransaction();
            $s=$db->prepare("INSERT INTO tb_vendas (cliente_id,data_venda,total_venda,observacoes) VALUES (?,NOW(),?,?)");
            $s->execute([$cliente?:null,$total,$obs]);
            $vendaId=$db->lastInsertId();
            $ins=$db->prepare("INSERT INTO tb_venda_itens (venda_id,produto_id,descricao,qtd,preco_unitario,subtotal) VALUES (?,?,?,?,?,?)");
            $upd=$db->prepare("UPDATE tb_produtos SET qtd_estoque=qtd_estoque-? WHERE id=?");
            foreach($itens as $i){
                $ins->execute([$vendaId,(int)$i['id'],$i['desc'],(float)$i['qtd'],(float)$i['preco'],(float)$i['sub']]);
                if((int)$i['id']>0) $upd->execute([(float)$i['qtd'],(int)$i['id']]);
            }
            $db->commit();
            $msg='<div class="alert alert-success">Venda #'.$vendaId.' finalizada com sucesso!</div>';
            echo '<script>localStorage.removeItem("pdvCarrinho");</script>';
        }catch(Exception $e){$db->rollBack();$msg='<div class="alert alert-danger">Erro: '.$e->getMessage().'</div>';}
    }
}

$formas=$db->query("SELECT * FROM formas_pgto ORDER BY nome")->fetchAll(PDO::FETCH_ASSOC);
?>
<style>
#pdvCarrinho tbody tr{cursor:pointer}
#pdvCarrinho tbody tr:hover{background:#fff3cd}
.prod-click{cursor:pointer}
.prod-click:hover{background:#e8f0fe}
.resumo-card{background:#f8f9fa;border-radius:8px;padding:15px}
</style>
<div class="container-fluid mt-3">
<h4><i class="bi bi-cart"></i> PDV - Ponto de Venda</h4>
<?=$msg?>
<form method="POST" id="formVenda">
<input type="hidden" name="itens_json" id="itensJson">
<input type="hidden" name="total" id="totalHidden">
<div class="row g-3">
  <!-- Coluna esquerda: produtos -->
  <div class="col-md-8">
    <div class="card"><div class="card-header py-2">
      <div class="row g-1">
        <div class="col-8"><input type="text" id="buscaProd" class="form-control form-control-sm" placeholder="Buscar produto..."></div>
        <div class="col-4"><select id="filtroCat" class="form-select form-select-sm"><option value="">Todas categorias</option>
          <?php foreach($db->query("SELECT id,nome FROM tb_categorias ORDER BY nome") as $c):?>
          <option value="<?=$c['id']?>"><?=htmlspecialchars($c['nome'])?></option>
          <?php endforeach;?>
        </select></div>
      </div>
    </div>
    <div class="card-body p-0" style="max-height:350px;overflow-y:auto">
      <table class="table table-sm table-hover mb-0" id="tblProdPDV">
        <thead class="table-light"><tr><th>Cód</th><th>Descrição</th><th>Preço</th><th>Estoque</th><th></th></tr></thead>
        <tbody id="prodBody">
          <?php foreach($db->query("SELECT p.*,c.nome as cat FROM tb_produtos p LEFT JOIN tb_categorias c ON p.categoria_id=c.id WHERE p.qtd_estoque>0 OR p.qtd_estoque IS NULL ORDER BY p.descricao LIMIT 50") as $p):?>
          <tr class="prod-click" onclick="addAoCarrinho(<?=$p['id']?>,'<?=htmlspecialchars($p['descricao'],ENT_QUOTES)?>',<?=$p['preco_venda']?:$p['preco']?>,<?=$p['qtd_estoque']?:0?>)">
            <td><?=$p['id']?></td>
            <td><?=htmlspecialchars($p['descricao'])?></td>
            <td>R$ <?=number_format($p['preco_venda']?:$p['preco'],2,',','.')?></td>
            <td><?=number_format($p['qtd_estoque']?:0,0,',','.')?></td>
            <td><button type="button" class="btn btn-sm btn-outline-success py-0">+</button></td>
          </tr>
          <?php endforeach;?>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Coluna direita: carrinho -->
  <div class="col-md-4">
    <div class="resumo-card">
      <div class="mb-2">
        <label class="small">Cliente</label>
        <input type="text" name="cliente" class="form-control form-control-sm" placeholder="Cliente (opcional)" id="edtCliente">
      </div>
      <div class="mb-2">
        <label class="small">Forma de Pagamento</label>
        <select name="forma_pgto" class="form-select form-select-sm">
          <?php foreach($formas as $f):?><option value="<?=$f['id']?>"><?=$f['nome']?></option><?php endforeach;?>
        </select>
      </div>
      <table class="table table-sm mb-2" id="pdvCarrinho">
        <thead><tr><th>Item</th><th>Qtd</th><th>R$</th><th></th></tr></thead>
        <tbody id="carrinhoBody">
          <tr id="carrinhoVazio"><td colspan="4" class="text-muted text-center small">Carrinho vazio</td></tr>
        </tbody>
      </table>
      <div class="d-flex justify-content-between fw-bold fs-5 mb-2">
        <span>Total:</span><span id="totalCarrinho">R$ 0,00</span>
      </div>
      <button type="submit" name="finalizar" class="btn btn-success w-100 mb-1" onclick="return finalizarVenda()">Finalizar Venda</button>
      <button type="button" class="btn btn-outline-danger w-100" onclick="limparVenda()">Limpar Venda</button>
    </div>
  </div>
</div>
</form>
</div>

<script>
var carrinho = JSON.parse(localStorage.getItem('pdvCarrinho') || '[]');
atualizarCarrinho();

function addAoCarrinho(id,desc,preco,estoque){
    var existente = carrinho.find(function(i){ return i.id===id; });
    if(existente){ existente.qtd = (existente.qtd||0) + 1; existente.sub = existente.qtd * existente.preco; }
    else { carrinho.push({id:id, desc:desc, preco:preco, qtd:1, sub:preco}); }
    localStorage.setItem('pdvCarrinho', JSON.stringify(carrinho));
    atualizarCarrinho();
}
function removerItem(idx){
    carrinho.splice(idx,1);
    localStorage.setItem('pdvCarrinho', JSON.stringify(carrinho));
    atualizarCarrinho();
}
function mudarQtd(idx,delta){
    if(carrinho[idx]){
        carrinho[idx].qtd = Math.max(0.5, (carrinho[idx].qtd||0) + delta);
        carrinho[idx].sub = carrinho[idx].qtd * carrinho[idx].preco;
        localStorage.setItem('pdvCarrinho', JSON.stringify(carrinho));
        atualizarCarrinho();
    }
}
function atualizarCarrinho(){
    var tbody = document.getElementById('carrinhoBody');
    if(carrinho.length===0){
        tbody.innerHTML = '<tr id="carrinhoVazio"><td colspan="4" class="text-muted text-center small">Carrinho vazio</td></tr>';
        document.getElementById('totalCarrinho').textContent = 'R$ 0,00';
        return;
    }
    var total = 0;
    var html = '';
    carrinho.forEach(function(item,idx){
        total += item.sub;
        html += '<tr><td>'+item.desc+'</td>'
            + '<td nowrap><button type="button" class="btn btn-sm py-0" onclick="mudarQtd('+idx+',-1)">−</button> '
            + (item.qtd).toLocaleString('pt-BR',{minimumFractionDigits:1})
            + ' <button type="button" class="btn btn-sm py-0" onclick="mudarQtd('+idx+',1)">+</button></td>'
            + '<td>R$ '+item.sub.toLocaleString('pt-BR',{minimumFractionDigits:2})+'</td>'
            + '<td><button type="button" class="btn btn-danger btn-sm py-0" onclick="removerItem('+idx+')">✕</button></td></tr>';
    });
    tbody.innerHTML = html;
    document.getElementById('totalCarrinho').textContent = 'R$ '+total.toLocaleString('pt-BR',{minimumFractionDigits:2});
}
function finalizarVenda(){
    if(carrinho.length===0){ alert('Adicione itens ao carrinho.'); return false; }
    document.getElementById('itensJson').value = JSON.stringify(carrinho);
    var total = carrinho.reduce(function(s,i){ return s+i.sub; }, 0);
    document.getElementById('totalHidden').value = total.toFixed(2).replace('.',',');
    return confirm('Confirmar venda de R$ '+total.toLocaleString('pt-BR',{minimumFractionDigits:2})+'?');
}
function limparVenda(){
    if(carrinho.length===0) return;
    if(!confirm('Limpar todos os itens do carrinho?')) return;
    carrinho = [];
    localStorage.removeItem('pdvCarrinho');
    atualizarCarrinho();
}
// Filtro de produtos
document.getElementById('buscaProd').addEventListener('input', function(){
    var q = this.value.toLowerCase();
    document.querySelectorAll('#prodBody tr').forEach(function(tr){
        tr.style.display = tr.cells[1].textContent.toLowerCase().indexOf(q)>=0 ? '' : 'none';
    });
});
document.getElementById('filtroCat').addEventListener('change', function(){
    var cat = this.value;
    // Em produção faria AJAX; por simplicidade filtra os já carregados
    document.querySelectorAll('#prodBody tr').forEach(function(tr){
        tr.style.display = (!cat || tr.dataset.cat===cat) ? '' : 'none';
    });
});
</script>
<?php include '../includes/footer.php';?>
