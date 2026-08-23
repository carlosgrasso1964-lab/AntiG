<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Projetos e Etapas';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$filtroStatus = $_GET['status'] ?? '';
$projetoId = $_GET['projeto_id'] ?? '';
$busca = $_GET['busca'] ?? '';

// === Filtro de status (auto-update) ===
if ($filtroStatus === 'Concluído' || $filtroStatus === 'concluidos') $filtroStatus = 'Concluído';
elseif ($filtroStatus === 'nao_concluidos') $filtroStatus = 'Em Andamento';

// === Auto-atualizar status (atrasado/atrasada) ===
$db->exec("UPDATE projetos SET status = 'Atrasado' WHERE status NOT IN ('Concluído','Cancelado') AND data_fim_prevista IS NOT NULL AND data_fim_prevista < NOW() AND (data_fim_realizada IS NULL OR data_fim_realizada > data_fim_prevista)");
$db->exec("UPDATE projetos SET status = 'Atrasado' WHERE status NOT IN ('Concluído','Cancelado','Atrasado') AND data_inicio_prevista IS NOT NULL AND data_inicio_prevista < NOW() AND data_inicio_realizada IS NULL");
$db->exec("UPDATE etapas SET status = 'Atrasada' WHERE status NOT IN ('Concluída','Cancelada') AND data_fim_prevista IS NOT NULL AND data_fim_prevista < NOW()");
$db->exec("UPDATE etapas SET status = 'Atrasada' WHERE status NOT IN ('Concluída','Cancelada','Atrasada') AND data_inicio_prevista IS NOT NULL AND data_inicio_prevista < NOW() AND data_inicio_realizada IS NULL");

// === Buscar projetos ===
$sqlProj = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, data_inicio_realizada, data_fim_realizada, status, valor_previsto_total, valor_realizado_total FROM projetos";
$params = [];
$wheres = [];
if ($filtroStatus) { $wheres[] = "status = ?"; $params[] = $filtroStatus; }
if ($projetoId) { $wheres[] = "id = ?"; $params[] = (int)$projetoId; }
if ($busca) { $wheres[] = "nome LIKE ?"; $params[] = "%$busca%"; }
if ($wheres) $sqlProj .= " WHERE " . implode(' AND ', $wheres);
$sqlProj .= " ORDER BY id";
$projetos = $db->prepare($sqlProj);
$projetos->execute($params);
$projetos = $projetos->fetchAll(PDO::FETCH_ASSOC);

// === Buscar etapas ===
$etapas = [];
if ($projetos) {
    $ids = array_column($projetos, 'id');
    $placeholders = implode(',', array_fill(0, count($ids), '?'));
    $stmt = $db->prepare("SELECT id, projeto_id, nome, prioridade, responsavel, data_inicio_prevista, data_fim_prevista, data_inicio_realizada, data_fim_realizada, status, observ, COALESCE(valor_previsto,0) as valor_previsto, COALESCE(valor_realizado,0) as valor_realizado FROM etapas WHERE projeto_id IN ($placeholders) ORDER BY id");
    $stmt->execute($ids);
    foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) as $e) {
        $etapas[$e['projeto_id']][] = $e;
    }
}

$msg = '';

// === AJAX: carregar projeto ===
if (isset($_GET['_ajax_projeto'])) {
    header('Content-Type: application/json');
    $id = (int)$_GET['_ajax_projeto'];
    $s = $db->prepare("SELECT * FROM projetos WHERE id=?");
    $s->execute([$id]);
    echo json_encode($s->fetch(PDO::FETCH_ASSOC));
    exit;
}
// === AJAX: carregar etapa ===
if (isset($_GET['_ajax_etapa'])) {
    header('Content-Type: application/json');
    $id = (int)$_GET['_ajax_etapa'];
    $s = $db->prepare("SELECT * FROM etapas WHERE id=?");
    $s->execute([$id]);
    echo json_encode($s->fetch(PDO::FETCH_ASSOC));
    exit;
}

// === CRUD handlers ===
$msg = '';
if (isset($_POST['salvar_projeto'])) {
    $nome = trim($_POST['nome']); $dip = $_POST['data_ini_prev'] ?: null; $dfp = $_POST['data_fim_prev'] ?: null;
    $dir = $_POST['data_ini_real'] ?: null; $dfr = $_POST['data_fim_real'] ?: null; $st = $_POST['status'] ?? 'Em Andamento';
    $vp = str_replace(',','.',str_replace('.','',$_POST['valor_prev'] ?? '0'));
    $vr = str_replace(',','.',str_replace('.','',$_POST['valor_real'] ?? '0'));
    $id = (int)($_POST['projeto_id'] ?? 0);
    if ($id) {
        $s=$db->prepare("UPDATE projetos SET nome=?,data_inicio_prevista=?,data_fim_prevista=?,data_inicio_realizada=?,data_fim_realizada=?,status=?,valor_previsto_total=?,valor_realizado_total=? WHERE id=?");
        $s->execute([$nome,$dip,$dfp,$dir,$dfr,$st,(float)$vp,(float)$vr,$id]);
        $msg = 'Projeto atualizado!';
    } else {
        $s=$db->prepare("INSERT INTO projetos (nome,data_inicio_prevista,data_fim_prevista,data_inicio_realizada,data_fim_realizada,status,valor_previsto_total,valor_realizado_total) VALUES (?,?,?,?,?,?,?,?)");
        $s->execute([$nome,$dip,$dfp,$dir,$dfr,$st,(float)$vp,(float)$vr]);
        $msg = 'Projeto criado!';
    }
}
if (isset($_POST['excluir_projeto'])) {
    $id=(int)($_POST['projeto_id']??0);
    $db->prepare("DELETE FROM etapas WHERE projeto_id=?")->execute([$id]);
    $db->prepare("DELETE FROM projetos WHERE id=?")->execute([$id]);
    $msg='Projeto excluído!';
}
if (isset($_POST['salvar_etapa'])) {
    $pid=(int)($_POST['projeto_id']??0); $nome=trim($_POST['nome']); $prio=$_POST['prioridade']??'';
    $resp=$_POST['responsavel']??''; $dip=$_POST['data_ini_prev']?:null; $dfp=$_POST['data_fim_prev']?:null;
    $dir=$_POST['data_ini_real']?:null; $dfr=$_POST['data_fim_real']?:null; $st=$_POST['status']??'Em Andamento';
    $obs=$_POST['observ']??''; $vp=str_replace(',','.',str_replace('.','',$_POST['valor_prev']??'0'));
    $vr=str_replace(',','.',str_replace('.','',$_POST['valor_real']??'0'));
    $id=(int)($_POST['etapa_id']??0);
    if ($id) {
        $s=$db->prepare("UPDATE etapas SET nome=?,prioridade=?,responsavel=?,data_inicio_prevista=?,data_fim_prevista=?,data_inicio_realizada=?,data_fim_realizada=?,status=?,observ=?,valor_previsto=?,valor_realizado=? WHERE id=?");
        $s->execute([$nome,$prio,$resp,$dip,$dfp,$dir,$dfr,$st,$obs,(float)$vp,(float)$vr,$id]);
        $msg='Etapa atualizada!';
    } else {
        $s=$db->prepare("INSERT INTO etapas (projeto_id,nome,prioridade,responsavel,data_inicio_prevista,data_fim_prevista,data_inicio_realizada,data_fim_realizada,status,observ,valor_previsto,valor_realizado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
        $s->execute([$pid,$nome,$prio,$resp,$dip,$dfp,$dir,$dfr,$st,$obs,(float)$vp,(float)$vr]);
        $msg='Etapa criada!';
    }
}
if (isset($_POST['excluir_etapa'])) {
    $id=(int)($_POST['etapa_id']??0);
    $db->prepare("DELETE FROM etapas WHERE id=?")->execute([$id]);
    $msg='Etapa excluída!';
}

function fmtData($d){ return $d ? date('d/m/Y', strtotime($d)) : ''; }
function fmtValor($v){ return $v ? number_format($v,2,',','.') : '0,00'; }
function perc($prev,$real){ if(!$prev||$prev==0)return '0,0%'; $p=$real/$prev*100; return number_format($p,1,',','.').'%'; }
?>
<style>.bg-projeto{background:#e8f0fe!important;font-weight:700}.bg-atrasado{background:#fce4ec!important}.bg-etapa td:first-child{padding-left:30px!important}</style>
<div class="container-fluid mt-3">
<h4>Projetos e Etapas</h4>
<?=$msg?$msg:''?>
<form method="GET" class="row g-2 mb-2 align-items-end">
  <div class="col-auto"><label class="small">Buscar</label><input type="text" name="busca" class="form-control form-control-sm" value="<?=htmlspecialchars($busca)?>" style="width:180px"></div>
  <div class="col-auto"><label class="small">ID Projeto</label><input type="number" name="projeto_id" class="form-control form-control-sm" value="<?=$projetoId?>" style="width:90px"></div>
  <div class="col-auto"><label class="small">Status</label>
    <select name="status" class="form-select form-select-sm" style="width:150px">
      <option value="">Todos</option>
      <option value="Em Andamento"<?=$filtroStatus==='Em Andamento'?' selected':''?>>Em Andamento</option>
      <option value="Concluído"<?=$filtroStatus==='Concluído'?' selected':''?>>Concluídos</option>
      <option value="Atrasado"<?=$filtroStatus==='Atrasado'?' selected':''?>>Atrasados</option>
      <option value="Cancelado"<?=$filtroStatus==='Cancelado'?' selected':''?>>Cancelados</option>
    </select>
  </div>
  <div class="col-auto"><button type="submit" class="btn btn-primary btn-sm">Filtrar</button>
    <a href="?" class="btn btn-outline-secondary btn-sm">Limpar</a>
    <a href="?status=Concluído" class="btn btn-success btn-sm">Concluídos</a>
    <a href="?status=Em Andamento" class="btn btn-warning btn-sm">Não Concluídos</a>
    <a href="#" class="btn btn-outline-secondary btn-sm" onclick="window.print()">Imprimir</a>
    <button type="button" class="btn btn-outline-info btn-sm" onclick="exibirGantt()">📊 Gantt</button>
    <button type="button" class="btn btn-outline-primary btn-sm" onclick="modalProjeto(0)">+ Novo Projeto</button>
  </div>
</form>
<div class="table-responsive">
<table class="table table-sm table-bordered table-striped" id="tblProjetos">
<thead class="table-light"><tr>
  <th>ID Projeto</th><th>Etapa</th><th>Prioridade</th><th>Responsável</th>
  <th>Início Prev.</th><th>Fim Prev.</th><th>Início Real</th><th>Fim Real</th>
  <th>Status</th><th>Observações</th><th class="text-end">R$ Previsto</th><th class="text-end">R$ Realizado</th><th class="text-end">%</th><th>Ações</th>
</tr></thead>
<tbody>
<?php if ($projetos): foreach ($projetos as $p):
  $totalPrev=0; $totalReal=0;
  $etapaRows = $etapas[$p['id']] ?? []; ?>
<tr class="bg-projeto">
  <td><strong><?=$p['id']?> - <?=htmlspecialchars($p['nome'])?></strong>
    <small class="text-muted d-block"><?=fmtData($p['data_inicio_prevista'])?> a <?=fmtData($p['data_fim_prevista'])?></small>
  </td>
  <td colspan="1"><em>Projeto</em></td>
  <td></td><td></td>
  <td><?=fmtData($p['data_inicio_prevista'])?></td>
  <td><?=fmtData($p['data_fim_prevista'])?></td>
  <td><?=fmtData($p['data_inicio_realizada'])?></td>
  <td><?=fmtData($p['data_fim_realizada'])?></td>
  <td><span class="badge bg-<?=$p['status']==='Concluído'?'success':($p['status']==='Atrasado'?'danger':'warning')?>"><?=$p['status']?></span></td>
  <td></td>
  <td class="text-end"><?=fmtValor($p['valor_previsto_total'])?></td>
  <td class="text-end"><?=fmtValor($p['valor_realizado_total'])?></td>
  <td class="text-end"><?=perc($p['valor_previsto_total'],$p['valor_realizado_total'])?></td>
  <td nowrap>
    <button class="btn btn-outline-primary btn-sm py-0" onclick="modalProjeto(<?=$p['id']?>)" title="Editar">✎</button>
    <button class="btn btn-outline-danger btn-sm py-0" onclick="excluirProjeto(<?=$p['id']?>)" title="Excluir">✕</button>
    <button class="btn btn-outline-success btn-sm py-0" onclick="modalEtapa(0,<?=$p['id']?>)" title="+ Etapa">+E</button>
  </td>
</tr>
<?php foreach ($etapaRows as $e): $totalPrev+=$e['valor_previsto']; $totalReal+=$e['valor_realizado']; ?>
<tr class="<?=$e['status']==='Atrasada'?'bg-atrasado':''?>">
  <td></td>
  <td><?=htmlspecialchars($e['nome'])?></td>
  <td><?=htmlspecialchars($e['prioridade'])?></td>
  <td><?=htmlspecialchars($e['responsavel'])?></td>
  <td><?=fmtData($e['data_inicio_prevista'])?></td>
  <td><?=fmtData($e['data_fim_prevista'])?></td>
  <td><?=fmtData($e['data_inicio_realizada'])?></td>
  <td><?=fmtData($e['data_fim_realizada'])?></td>
  <td><span class="badge bg-<?=$e['status']==='Concluída'?'success':($e['status']==='Atrasada'?'danger':'warning')?>"><?=$e['status']?></span></td>
  <td style="max-width:120px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="<?=htmlspecialchars($e['observ']??'')?>"><?=htmlspecialchars(substr($e['observ']??'',0,30))?></td>
  <td class="text-end"><?=fmtValor($e['valor_previsto'])?></td>
  <td class="text-end"><?=fmtValor($e['valor_realizado'])?></td>
  <td class="text-end"><?=perc($e['valor_previsto'],$e['valor_realizado'])?></td>
  <td nowrap>
    <button class="btn btn-outline-primary btn-sm py-0" onclick="modalEtapa(<?=$e['id']?>,<?=$p['id']?>)" title="Editar">✎</button>
    <button class="btn btn-outline-danger btn-sm py-0" onclick="excluirEtapa(<?=$e['id']?>)" title="Excluir">✕</button>
  </td>
</tr>
<?php endforeach; ?>
<?php endforeach; else: ?>
<tr><td colspan="14" class="text-center text-muted">Nenhum projeto encontrado.</td></tr>
<?php endif; ?>
</tbody>
</table>
</div></div>

<!-- Modal Projeto -->
<div class="modal fade" id="modalProjeto"><div class="modal-dialog"><form method="POST" class="modal-content">
<div class="modal-header"><h5 class="modal-title">Projeto</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
<div class="modal-body row g-2">
  <input type="hidden" name="projeto_id" id="projId">
  <div class="col-12"><label>Nome</label><input type="text" name="nome" id="projNome" class="form-control form-control-sm" required></div>
  <div class="col-6"><label>Início Prev.</label><input type="date" name="data_ini_prev" id="projDIP" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Fim Prev.</label><input type="date" name="data_fim_prev" id="projDFP" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Início Real</label><input type="date" name="data_ini_real" id="projDIR" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Fim Real</label><input type="date" name="data_fim_real" id="projDFR" class="form-control form-control-sm"></div>
  <div class="col-4"><label>Status</label><select name="status" id="projStatus" class="form-select form-select-sm">
    <option>Em Andamento</option><option>Concluído</option><option>Atrasado</option><option>Cancelado</option>
  </select></div>
  <div class="col-4"><label>Previsto R$</label><input type="text" name="valor_prev" id="projVP" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-4"><label>Realizado R$</label><input type="text" name="valor_real" id="projVR" class="form-control form-control-sm" value="0,00"></div>
</div>
<div class="modal-footer">
  <button type="submit" name="salvar_projeto" class="btn btn-primary">Salvar</button>
  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
</div>
</form></div></div>

<!-- Modal Etapa -->
<div class="modal fade" id="modalEtapa"><div class="modal-dialog"><form method="POST" class="modal-content">
<div class="modal-header"><h5 class="modal-title">Etapa</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
<div class="modal-body row g-2">
  <input type="hidden" name="etapa_id" id="etpId">
  <input type="hidden" name="projeto_id" id="etpProjId">
  <div class="col-12"><label>Nome</label><input type="text" name="nome" id="etpNome" class="form-control form-control-sm" required></div>
  <div class="col-4"><label>Prioridade</label><select name="prioridade" id="etpPrio" class="form-select form-select-sm"><option>Baixa</option><option selected>Média</option><option>Alta</option><option>Urgente</option></select></div>
  <div class="col-8"><label>Responsável</label><input type="text" name="responsavel" id="etpResp" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Início Prev.</label><input type="date" name="data_ini_prev" id="etpDIP" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Fim Prev.</label><input type="date" name="data_fim_prev" id="etpDFP" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Início Real</label><input type="date" name="data_ini_real" id="etpDIR" class="form-control form-control-sm"></div>
  <div class="col-6"><label>Fim Real</label><input type="date" name="data_fim_real" id="etpDFR" class="form-control form-control-sm"></div>
  <div class="col-4"><label>Status</label><select name="status" id="etpStatus" class="form-select form-select-sm">
    <option>Em Andamento</option><option>Concluída</option><option>Atrasada</option><option>Cancelada</option>
  </select></div>
  <div class="col-4"><label>Previsto R$</label><input type="text" name="valor_prev" id="etpVP" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-4"><label>Realizado R$</label><input type="text" name="valor_real" id="etpVR" class="form-control form-control-sm" value="0,00"></div>
  <div class="col-12"><label>Observações</label><textarea name="observ" id="etpObs" class="form-control form-control-sm" rows="2"></textarea></div>
</div>
<div class="modal-footer">
  <button type="submit" name="salvar_etapa" class="btn btn-primary">Salvar</button>
  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
</div>
</form></div></div>

<!-- Modal Gantt -->
<div class="modal fade modal-xl" id="modalGantt"><div class="modal-dialog modal-xl"><div class="modal-content">
<div class="modal-header"><h5 class="modal-title">Gráfico de Gantt - Cronograma</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
<div class="modal-body" id="ganttBody" style="overflow-x:auto;min-height:300px"></div>
</div></div></div>

<script>
function modalProjeto(id){
  document.getElementById('projId').value=id||0;
  ['projNome','projDIP','projDFP','projDIR','projDFR','projStatus','projVP','projVR'].forEach(function(f){document.getElementById(f).value='';});
  if(id){
    var row=document.querySelector('#tblProjetos tbody tr[data-id="'+id+'"]');
    fetch('?_ajax_projeto='+id).then(function(r){return r.json();}).then(function(p){
      if(!p)return;
      document.getElementById('projNome').value=p.nome;
      if(p.data_inicio_prevista) document.getElementById('projDIP').value=p.data_inicio_prevista.substring(0,10);
      if(p.data_fim_prevista) document.getElementById('projDFP').value=p.data_fim_prevista.substring(0,10);
      if(p.data_inicio_realizada) document.getElementById('projDIR').value=p.data_inicio_realizada.substring(0,10);
      if(p.data_fim_realizada) document.getElementById('projDFR').value=p.data_fim_realizada.substring(0,10);
      document.getElementById('projStatus').value=p.status||'Em Andamento';
      document.getElementById('projVP').value=(p.valor_previsto_total||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
      document.getElementById('projVR').value=(p.valor_realizado_total||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
    });
  }
  new bootstrap.Modal(document.getElementById('modalProjeto')).show();
}
function excluirProjeto(id){ if(confirm('Excluir projeto e todas as suas etapas?')){ var f=document.createElement('form');f.method='POST';f.innerHTML='<input name="excluir_projeto" value="1"><input name="projeto_id" value="'+id+'">';document.body.appendChild(f);f.submit();}}
function modalEtapa(id,projId){
  document.getElementById('etpId').value=id||0;
  document.getElementById('etpProjId').value=projId;
  ['etpNome','etpPrio','etpResp','etpDIP','etpDFP','etpDIR','etpDFR','etpStatus','etpVP','etpVR','etpObs'].forEach(function(f){document.getElementById(f).value='';});
  if(id){
    fetch('?_ajax_etapa='+id).then(function(r){return r.json();}).then(function(e){
      if(!e)return;
      document.getElementById('etpNome').value=e.nome;
      document.getElementById('etpPrio').value=e.prioridade||'Média';
      document.getElementById('etpResp').value=e.responsavel||'';
      if(e.data_inicio_prevista) document.getElementById('etpDIP').value=e.data_inicio_prevista.substring(0,10);
      if(e.data_fim_prevista) document.getElementById('etpDFP').value=e.data_fim_prevista.substring(0,10);
      if(e.data_inicio_realizada) document.getElementById('etpDIR').value=e.data_inicio_realizada.substring(0,10);
      if(e.data_fim_realizada) document.getElementById('etpDFR').value=e.data_fim_realizada.substring(0,10);
      document.getElementById('etpStatus').value=e.status||'Em Andamento';
      document.getElementById('etpVP').value=(e.valor_previsto||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
      document.getElementById('etpVR').value=(e.valor_realizado||0).toLocaleString('pt-BR',{minimumFractionDigits:2});
      document.getElementById('etpObs').value=e.observ||'';
    });
  }
  new bootstrap.Modal(document.getElementById('modalEtapa')).show();
}
function excluirEtapa(id){ if(confirm('Excluir etapa?')){ var f=document.createElement('form');f.method='POST';f.innerHTML='<input name="excluir_etapa" value="1"><input name="etapa_id" value="'+id+'">';document.body.appendChild(f);f.submit();}}
function exibirGantt(){
  var tbl=document.getElementById('tblProjetos');
  if(!tbl||tbl.rows.length<2){alert('Sem dados para o gráfico.');return;}
  // Parse rows: project rows (class bg-projeto) and etapa rows
  var rows=[];
  for(var i=1;i<tbl.rows.length;i++){
    var r=tbl.rows[i];
    var cells=r.cells;
    if(cells.length<8)continue;
    var isProj=r.classList.contains('bg-projeto');
    var name=isProj?cells[0].textContent.trim():cells[1].textContent.trim();
    var dip=cells[4].textContent.trim(),dfp=cells[5].textContent.trim();
    var dir=cells[6].textContent.trim(),dfr=cells[7].textContent.trim();
    var status=cells[8].textContent.trim();
    if(!dip&&!dfp)continue;
    // Use realizadas if available, else previstas
    var start=dir||dip, end=dfr||dfp;
    if(!start||!end)continue;
    var parts=start.split('/'); if(parts.length!==3)continue;
    var d1=new Date(parts[2],parts[1]-1,parts[0]);
    parts=end.split('/'); if(parts.length!==3)continue;
    var d2=new Date(parts[2],parts[1]-1,parts[0]);
    rows.push({name:name,start:d1,end:d2,status:status,isProj:isProj});
  }
  if(!rows.length){alert('Nenhum item com datas para exibir no Gantt.');return;}
  // Calculate date range
  var minDate=rows[0].start,maxDate=rows[0].end;
  rows.forEach(function(r){
    if(r.start<minDate)minDate=r.start;
    if(r.end>maxDate)maxDate=r.end;
  });
  // Add margin
  var margin=Math.ceil((maxDate-minDate)/20);
  minDate=new Date(minDate-margin); maxDate=new Date(maxDate+margin);
  var totalDays=Math.ceil((maxDate-minDate)/(1000*60*60*24));
  // Build header: month labels
  var months=[];
  var d=new Date(minDate);
  while(d<=maxDate){
    var m=d.getMonth(),y=d.getFullYear();
    var label=String(m+1).padStart(2,'0')+'/'+y;
    var monthStart=new Date(y,m,1);
    if(monthStart<minDate)monthStart=minDate;
    var monthEnd=new Date(y,m+1,0,23,59,59);
    if(monthEnd>maxDate)monthEnd=maxDate;
    var leftPct=((monthStart-minDate)/(1000*60*60*24))/totalDays*100;
    var widthPct=((monthEnd-monthStart)/(1000*60*60*24)+1)/totalDays*100;
    months.push({label:label,left:leftPct,width:widthPct});
    d.setMonth(d.getMonth()+1);
  }
  // Today marker position
  var today=new Date();today.setHours(0,0,0,0);
  var todayPct=today>=minDate&&today<=maxDate?((today-minDate)/(1000*60*60*24))/totalDays*100:-1;
  // Status colors
  var colors={'Concluído':'#28a745','Concluída':'#28a745','Em Andamento':'#007bff','Atrasado':'#dc3545','Atrasada':'#dc3545','Cancelado':'#6c757d','Cancelada':'#6c757d'};
  // Render
  var barHeight=28,rowGap=2,headerH=30,leftColW=320;
  var PX=Math.min(totalDays*3,950); // limita largura do eixo temporal
  var totalH=headerH+rows.length*(barHeight+rowGap)+20;
  var html='<div style="position:relative;min-width:'+(leftColW+PX)+'px;height:'+totalH+'px;font-size:12px">';
  // Left column header
  html+='<div style="position:absolute;top:0;left:0;width:'+leftColW+'px;height:'+headerH+'px;background:#f8f9fa;border-bottom:1px solid #dee2e6;font-weight:700;padding:5px 10px">Item</div>';
  // Timeline header (months)
  months.forEach(function(m){
    html+='<div style="position:absolute;top:0;left:'+(leftColW+m.left/100*PX)+'px;width:'+(m.width/100*PX)+'px;height:'+headerH+'px;background:#f8f9fa;border-bottom:1px solid #dee2e6;border-left:1px solid #dee2e6;text-align:center;font-size:11px;padding-top:6px">'+m.label+'</div>';
  });
  // Rows
  var y=headerH;
  for(var i=0;i<rows.length;i++){
    var r=rows[i];
    var startPct=((r.start-minDate)/(1000*60*60*24))/totalDays*100;
    var barW=Math.max(((r.end-r.start)/(1000*60*60*24)+1)/totalDays*100,2);
    var barX=startPct/100*PX;
    var barWpx=barW/100*PX;
    var bg=r.isProj?'#e8f0fe':'#fff3cd';
    var barColor=colors[r.status]||'#6c757d';
    html+='<div style="position:absolute;top:'+y+'px;left:0;width:'+leftColW+'px;height:'+barHeight+'px;background:'+bg+';padding:3px 10px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;border-bottom:1px solid #f0f0f0;font-weight:'+(r.isProj?'700':'400')+'">'+r.name+'</div>';
    html+='<div style="position:absolute;top:'+(y+3)+'px;left:'+(leftColW+barX)+'px;width:'+barWpx+'px;height:'+(barHeight-6)+'px;background:'+barColor+';border-radius:4px;opacity:0.85" title="'+r.name+': '+r.start.toLocaleDateString('pt-BR')+' a '+r.end.toLocaleDateString('pt-BR')+'"></div>';
    y+=barHeight+rowGap;
  }
  // Today line
  if(todayPct>=0){
    var tx=leftColW+todayPct/100*PX;
    html+='<div style="position:absolute;top:0;left:'+tx+'px;width:2px;height:'+totalH+'px;background:#dc3545;z-index:5" title="Hoje"></div>';
  }
  html+='</div>';
  document.getElementById('ganttBody').innerHTML=html;
  new bootstrap.Modal(document.getElementById('modalGantt')).show();
}
</script>
<?php include '../includes/footer.php'; ?>
