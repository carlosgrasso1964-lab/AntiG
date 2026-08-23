<?php
session_start(); ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php'; require_once '../includes/functions.php';
requireAuth(); $title='PDC - Ponto de Compra'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
$msg='';
if(isset($_POST['finalizar'])){
    $forn=trim($_POST['fornecedor']??''); $obs=$_POST['obs']??'';
    $itens=json_decode($_POST['itens_json']??'[]',true);
    $total=(float)str_replace(',','.',$_POST['total']??'0');
    if(count($itens)==0){$msg='<div class="alert alert-danger">Adicione itens.</div>';}
    else {
        try{
            $db->beginTransaction();
            $s=$db->prepare("INSERT INTO tb_compras (fornecedor_id,data_compra,total_compra,observacoes) VALUES (?,NOW(),?,?)");
            $s->execute([$forn?:null,$total,$obs]);
            $compraId=$db->lastInsertId();
            $ins=$db->prepare("INSERT INTO tb_compra_itens (compra_id,produto_id,descricao,qtd,preco_unitario,subtotal) VALUES (?,?,?,?,?,?)");
            $updEst=$db->prepare("UPDATE tb_produtos SET qtd_estoque=qtd_estoque+? WHERE id=?");
            $busca=$db->prepare("SELECT qtd_estoque,COALESCE(precoMedioCusto,preco) as custo_atual FROM tb_produtos WHERE id=?");
            $updCusto=$db->prepare("UPDATE tb_produtos SET precoMedioCusto=?, preco=? WHERE id=?");
            foreach($itens as $i){
                $pid=(int)$i['id']; $qtd=(float)$i['qtd']; $preco=(float)$i['preco']; $sub=$qtd*$preco;
                $ins->execute([$compraId,$pid,$i['desc'],$qtd,$preco,$sub]);
                if($pid>0){
                    $updEst->execute([$qtd,$pid]);
                    // Custo médio ponderado: ((custo_atual * estoque_anterior) + (preco_compra * qtd)) / (estoque_anterior + qtd)
                    $busca->execute([$pid]); $r=$busca->fetch(PDO::FETCH_ASSOC);
                    $estAnt=(float)$r['qtd_estoque'] - $qtd; // estoque antes da atualização
                    $custoAtual=(float)$r['custo_atual'];
                    $novoCusto=$estAnt>0?(($custoAtual*$estAnt)+($preco*$qtd))/($estAnt+$qtd):$preco;
                    $updCusto->execute([$novoCusto,$preco,$pid]);
                }
            }
            $db->commit();
            $msg='<div class="alert alert-success">Compra #'.$compraId.' registrada! Custo médio recalculado.</div>';
            echo '<script>localStorage.removeItem("pdcCarrinho");</script>';
        }catch(Exception $e){$db->rollBack();$msg='<div class="alert alert-danger">Erro: '.$e->getMessage().'</div>';}
    }
}
?>
<div class="container-fluid mt-3">
<h4><i class="bi bi-truck"></i> PDC - Ponto de Compra</h4><?=$msg?>
<form method="POST" id="formCompra">
<input type="hidden" name="itens_json" id="itensJson">
<input type="hidden" name="total" id="totalHidden">
<div class="row g-3">
  <div class="col-md-8">
    <div class="card"><div class="card-header py-2">
      <input type="text" id="buscaProd" class="form-control form-control-sm" placeholder="Buscar produto...">
    </div>
    <div class="card-body p-0" style="max-height:350px;overflow-y:auto">
      <table class="table table-sm table-hover mb-0">
        <thead class="table-light"><tr><th>Cód</th><th>Descrição</th><th>Custo Atual</th><th>Preço Venda</th><th>Estoque</th><th></th></tr></thead>
        <tbody id="prodBody">
          <?php foreach($db->query("SELECT *,(SELECT COALESCE(precoMedioCusto,preco) FROM tb_produtos p2 WHERE p2.id=p.id) as custo FROM tb_produtos p ORDER BY descricao LIMIT 50") as $p):?>
          <tr class="prod-click" onclick="addAoCarrinho(<?=$p['id']?>,'<?=htmlspecialchars($p['descricao'],ENT_QUOTES)?>',<?=$p['preco']?:0?>,<?=$p['custo']?:0?>)">
            <td><?=$p['id']?></td>
            <td><?=htmlspecialchars($p['descricao'])?></td>
            <td>R$ <?=number_format($p['custo']?:$p['preco'],2,',','.')?></td>
            <td>R$ <?=number_format($p['preco_venda']?:0,2,',','.')?></td>
            <td><?=number_format($p['qtd_estoque']?:0,0,',','.')?></td>
            <td><button type="button" class="btn btn-sm btn-outline-primary py-0">+</button></td>
          </tr>
          <?php endforeach;?>
        </tbody>
      </table>
    </div></div>
  </div>
  <div class="col-md-4">
    <div class="resumo-card p-3 bg-light rounded">
      <div class="mb-2">
        <label class="small">Fornecedor</label>
        <input type="text" name="fornecedor" class="form-control form-control-sm" placeholder="Fornecedor (opcional)">
      </div>
      <table class="table table-sm mb-2" id="carrinhoTable">
        <thead><tr><th>Item</th><th>Qtd</th><th>R$ Und</th><th>Sub</th><th></th></tr></thead>
        <tbody id="carrinhoBody"><tr id="carrinhoVazio"><td colspan="5" class="text-muted text-center small">Carrinho vazio</td></tr></tbody>
      </table>
      <div class="d-flex justify-content-between fw-bold fs-5 mb-2">
        <span>Total:</span><span id="totalCarrinho">R$ 0,00</span>
      </div>
      <button type="submit" name="finalizar" class="btn btn-primary w-100 mb-1" onclick="return finalizarCompra()">Registrar Compra</button>
      <button type="button" class="btn btn-outline-danger w-100" onclick="limparCarrinho()">Limpar</button>
    </div>
  </div>
</div>
</form></div>
<script>
var carrinho = JSON.parse(localStorage.getItem('pdcCarrinho') || '[]');
atualizarCarrinho();
function addAoCarrinho(id,desc,preco,custo){
    var e=carrinho.find(function(i){return i.id===id;});
    if(e){e.qtd=(e.qtd||0)+1;e.sub=e.qtd*e.preco;}
    else{carrinho.push({id:id,desc:desc,preco:custo||preco,qtd:1,sub:(custo||preco)});}
    localStorage.setItem('pdcCarrinho',JSON.stringify(carrinho)); atualizarCarrinho();
}
function removerItem(idx){carrinho.splice(idx,1);localStorage.setItem('pdcCarrinho',JSON.stringify(carrinho));atualizarCarrinho();}
function mudarPreco(idx,val){
    var v=parseFloat(val.replace(',','.')); if(isNaN(v)||v<0)return;
    carrinho[idx].preco=v; carrinho[idx].sub=carrinho[idx].qtd*v;
    localStorage.setItem('pdcCarrinho',JSON.stringify(carrinho));atualizarCarrinho();
}
function mudarQtd(idx,delta){
    carrinho[idx].qtd=Math.max(0.5,(carrinho[idx].qtd||0)+delta);
    carrinho[idx].sub=carrinho[idx].qtd*carrinho[idx].preco;
    localStorage.setItem('pdcCarrinho',JSON.stringify(carrinho));atualizarCarrinho();
}
function atualizarCarrinho(){
    if(!carrinho.length){document.getElementById('carrinhoBody').innerHTML='<tr id="carrinhoVazio"><td colspan="5" class="text-muted text-center small">Carrinho vazio</td></tr>';document.getElementById('totalCarrinho').textContent='R$ 0,00';return;}
    var total=0,html='';
    carrinho.forEach(function(item,idx){
        total+=item.sub;
        html+='<tr><td>'+item.desc+'</td>'
            +'<td nowrap><button type="button" class="btn btn-sm py-0" onclick="mudarQtd('+idx+',-1)">−</button> '+(item.qtd).toLocaleString('pt-BR',{minimumFractionDigits:1})
            +' <button type="button" class="btn btn-sm py-0" onclick="mudarQtd('+idx+',1)">+</button></td>'
            +'<td><input type="text" class="form-control form-control-sm" style="width:80px" value="'+item.preco.toLocaleString('pt-BR',{minimumFractionDigits:2})+'" onchange="mudarPreco('+idx+',this.value)"></td>'
            +'<td>R$ '+item.sub.toLocaleString('pt-BR',{minimumFractionDigits:2})+'</td>'
            +'<td><button type="button" class="btn btn-danger btn-sm py-0" onclick="removerItem('+idx+')">✕</button></td></tr>';
    });
    document.getElementById('carrinhoBody').innerHTML=html;
    document.getElementById('totalCarrinho').textContent='R$ '+total.toLocaleString('pt-BR',{minimumFractionDigits:2});
}
function finalizarCompra(){
    if(!carrinho.length){alert('Adicione itens.');return false;}
    document.getElementById('itensJson').value=JSON.stringify(carrinho);
    var total=carrinho.reduce(function(s,i){return s+i.sub;},0);
    document.getElementById('totalHidden').value=total.toFixed(2).replace('.',',');
    return confirm('Registrar compra de R$ '+total.toLocaleString('pt-BR',{minimumFractionDigits:2})+'?');
}
function limparCarrinho(){if(carrinho.length&&confirm('Limpar carrinho?')){carrinho=[];localStorage.removeItem('pdcCarrinho');atualizarCarrinho();}}
document.getElementById('buscaProd').addEventListener('input',function(){
    var q=this.value.toLowerCase();
    document.querySelectorAll('#prodBody tr').forEach(function(tr){tr.style.display=tr.cells[1].textContent.toLowerCase().indexOf(q)>=0?'':'none';});
});
</script>
<?php include '../includes/footer.php';?>
