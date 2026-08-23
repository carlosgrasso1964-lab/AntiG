 $(document).ready(function() {                                                                                                                                                                                                                      
         var dp = $('#modalMov').length ? $('#modalMov') : undefined;                                                                                                                                                                                    
         $('.select2').select2({                                                                                                                                                                                                                         
             theme: 'bootstrap-5',                                                                                                                                                                                                                       
             width: '100%',                                                                                                                                                                                                                              
             dropdownParent: dp                                                                                                                                                                                                                          
         });

    $('.datatable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/2.2.2/i18n/pt-BR.json'
        },
        pageLength: 25,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "Todos"]],
        ordering: false
    });

    $('.money').on('input', function() {
        let value = $(this).val().replace(/\D/g, '');
        value = (value / 100).toFixed(2) + '';
        value = value.replace('.', ',');
        value = value.replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.');
        $(this).val(value);
    });

    $('.money').on('blur', function() {
        let value = $(this).val();
        if (value === '' || value === '0,00') {
            $(this).val('');
        }
    });

    $('.datepicker').on('input', function() {
        let value = $(this).val().replace(/\D/g, '');
        if (value.length > 8) value = value.substr(0, 8);
        if (value.length >= 5) {
            value = value.substr(0, 2) + '/' + value.substr(2, 2) + '/' + value.substr(4);
        } else if (value.length >= 3) {
            value = value.substr(0, 2) + '/' + value.substr(2);
        }
        $(this).val(value);
    });

    $('[data-confirm]').on('click', function(e) {
        e.preventDefault();
        var msg = $(this).data('confirm') || 'Tem certeza?';
        var href = $(this).attr('href');
        Swal.fire({
            title: 'Confirmação',
            text: msg,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            confirmButtonText: 'Sim, confirmar!',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) window.location.href = href;
        });
    });

    // === FUNCIONALIDADES DOS LANÇAMENTOS ===

    // Preencher campos usando dados JSON do PHP (sem depender de data-*)
    function preencherCamposRecurso() {
        var val = document.getElementById('recurso').value;
        document.getElementById('tfNrRecurso').value = val || '';
        if (val && typeof recursosData !== 'undefined') {
            var item = recursosData.find(function(r) { return r.codigo === val; });
            if (item) {
                var fk = item.fk_gpprinc || '';
                var nome = item.nomebco || '';
                document.getElementById('tfRecurso').value = fk ? fk + ' - ' + nome : nome;
                document.getElementById('vrecurso').value = fk;
                var div = document.getElementById('bancoDestinoFields');
                if (fk === '2.001.004' || nome.toUpperCase().indexOf('EMPRÉSTIMOS') >= 0) {
                    div.classList.remove('d-none');
                } else {
                    div.classList.add('d-none');
                }
                // Cartão de crédito (2.001.003) desconsidera o Tipo (sempre Saída) — espelha VMov.java
                var tipoEl = document.getElementById('tipo');
                if (tipoEl) {
                    if (fk === '2.001.003') {
                        tipoEl.value = 'S';
                        tipoEl.disabled = true;
                    } else {
                        tipoEl.disabled = false;
                    }
                }
                // Cartão de crédito: se Apresentação estiver vazia, copia a data do Vencimento
                // (espelha o fluxo do VMov.java, onde dtApr = dtVcto nos 2 primeiros lançamentos)
                window.__cartaoCreditoAtivo = (fk === '2.001.003');
                if (fk === '2.001.003') {
                    var vcto = document.querySelector('input[name="dtVcto"]');
                    var apr = document.querySelector('input[name="dtApr"]');
                    if (vcto && apr && !apr.value.trim()) {
                        apr.value = vcto.value;
                    }
                }
                return;
            }
        }
        window.__cartaoCreditoAtivo = false;
        document.getElementById('tfRecurso').value = val;
        document.getElementById('vrecurso').value = '';
        document.getElementById('bancoDestinoFields').classList.add('d-none');
    }

    function preencherCamposFavorecido() {
        var val = document.getElementById('clifor').value;
        document.getElementById('tfNrFav').value = val || '';
        if (val && typeof cliforsData !== 'undefined') {
            var item = cliforsData.find(function(c) { return c.codCliFor === val; });
            if (item) {
                var fkgp = item.fkCliForGp || '';
                var nome = item.nomeCliFor || '';
                document.getElementById('tfFavorecidos').value = fkgp ? fkgp + ' - ' + nome : nome;
                document.getElementById('vCliFor').value = fkgp;
                return;
            }
        }
        document.getElementById('tfFavorecidos').value = val;
        document.getElementById('vCliFor').value = '';
    }

    function preencherCamposClassif() {
        var val = document.getElementById('classif').value;
        if (val && typeof planosData !== 'undefined') {
            var item = planosData.find(function(p) { return p.cod_Geral === val; });
            if (item) {
                document.getElementById('tfClass').value = val + ' - ' + (item.nome_C || '');
                return;
            }
        }
        document.getElementById('tfClass').value = val || '';
    }

    // Garantir preenchimento ao selecionar (usando jQuery para compatibilidade com Select2)
       $('#recurso').on('change select2:select', preencherCamposRecurso);
       $('#clifor').on('change select2:select', preencherCamposFavorecido);
       $('#classif').on('change select2:select', preencherCamposClassif);

    // Cartão de crédito: ao sair do campo Vencimento, se Apresentação estiver vazia,
    // copia a data do vencimento (espelha VMov.java — dtApr = dtVcto nas 2 primeiras etapas)
    $(document).on('change blur', 'input[name="dtVcto"]', function() {
        if (window.__cartaoCreditoAtivo) {
            var apr = document.querySelector('input[name="dtApr"]');
            if (apr && !apr.value.trim()) {
                apr.value = this.value;
            }
        }
    });

    // Ao abrir o modal em edição, preencher os campos
    $('#modalMov').on('shown.bs.modal', function() {
        preencherCamposRecurso();
        preencherCamposFavorecido();
        preencherCamposClassif();
    });

    // Pré-popular no momento do submit (garantia final)
    $('#modalMov form').on('submit', function() {
        preencherCamposRecurso();
        preencherCamposFavorecido();
        preencherCamposClassif();
    });

    // Check All
    $('#checkAll').on('change', function() {
        $('.checkItem').prop('checked', $(this).is(':checked'));
        toggleDeleteButton();
    });

    $(document).on('change', '.checkItem', function() {
        toggleDeleteButton();
        var allChecked = $('.checkItem:checked').length === $('.checkItem').length;
        $('#checkAll').prop('checked', allChecked);
    });

    // Botão Excluir Selecionados
    $('#btnDeleteSelected').on('click', function() {
        var checked = $('.checkItem:checked');
        if (checked.length === 0) {
            Swal.fire('Atenção', 'Selecione ao menos um lançamento.', 'warning');
            return;
        }
        Swal.fire({
            title: 'Excluir ' + checked.length + ' lançamento(s)?',
            text: 'Esta ação não pode ser desfeita.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            confirmButtonText: 'Sim, excluir!',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                $('#formMultipleDelete').submit();
            }
        });
    });

    // Filtro por Recurso (comportamento Enter)
    $('#filtroRecurso').on('change', function() {
        filtrarPorRecurso();
    });
});

function toggleDeleteButton() {
    var checked = $('.checkItem:checked').length;
    if (checked > 0) {
        $('#btnDeleteSelected').removeClass('d-none').text('Excluir Selecionados (' + checked + ')');
    } else {
        $('#btnDeleteSelected').addClass('d-none');
    }
}

function filtrarPorRecurso() {
    var recurso = $('#filtroRecurso').val();
    var url = new URL(window.location.href);
    if (recurso) {
        url.searchParams.set('filtro_recurso', recurso);
    } else {
        url.searchParams.delete('filtro_recurso');
    }
    url.searchParams.delete('pagina');
    window.location.href = url.toString();
}

function filtrarPorTipo() {
    var tipo = $('#filtroTipo').val();
    var url = new URL(window.location.href);
    if (tipo && tipo !== 'T') {
        url.searchParams.set('tipo', tipo);
    } else {
        url.searchParams.delete('tipo');
    }
    url.searchParams.delete('pagina');
    window.location.href = url.toString();
}

function filtrarPorOrdem() {
    var ordem = $('#filtroOrdem').val();
    var url = new URL(window.location.href);
    if (ordem && ordem !== 'vencimento') {
        url.searchParams.set('ordem', ordem);
    } else {
        url.searchParams.delete('ordem');
    }
    url.searchParams.delete('pagina');
    window.location.href = url.toString();
}

function moneyToFloat(value) {
    if (!value) return 0;
    return parseFloat(value.replace(/\./g, '').replace(',', '.')) || 0;
}

function floatToMoney(value) {
    return value.toFixed(2).replace('.', ',').replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.');
}

function confirmDelete(message) {
    return Swal.fire({
        title: 'Confirmação',
        text: message || 'Deseja realmente excluir?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        confirmButtonText: 'Sim, excluir!',
        cancelButtonText: 'Cancelar'
    });
}

// ===== Tema claro/escuro =====
function alternarTema() {
    var atual = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    var novo = atual === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-bs-theme', novo);
    localStorage.setItem('jfipTheme', novo);
    atualizarIconeTema(novo);
}

function atualizarIconeTema(tema) {
    var btn = document.getElementById('btnTema');
    if (!btn) return;
    var icone = btn.querySelector('i');
    if (icone) {
        icone.className = tema === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
    }
}

// Sincroniza o ícone com o tema já aplicado no <html> pelo script do header
document.addEventListener('DOMContentLoaded', function () {
    atualizarIconeTema(document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light');
});
