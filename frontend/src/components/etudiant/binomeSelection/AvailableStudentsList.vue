<template>
    <Card class="table-card">
        <template #title>
            <div class="card-header">
                <h3>Étudiants Disponibles</h3>
                <Badge
                    :value="availableStudents.length.toString()"
                    severity="info"
                />
            </div>
        </template>
        <template #content>
            <div v-if="hasSentRequest" class="sent-request-info">
                <i class="pi pi-clock"></i>
                <h3>Demande en attente</h3>
                <p>
                    Vous avez déjà envoyé une demande qui est en
                    attente de réponse.
                </p>
                <Button
                    label="Annuler la demande"
                    icon="pi pi-times"
                    class="p-button-outlined p-button-danger mt-3"
                    @click="$emit('cancel-request')"
                    :loading="processingCancel"
                    :disabled="processingCancel"
                />
            </div>
            <div v-else>
                <div v-if="loading" class="loading-container">
                    <ProgressSpinner />
                    <p>Chargement des étudiants disponibles...</p>
                </div>
                <div
                    v-else-if="availableStudents.length === 0"
                    class="empty-state"
                >
                    <i class="pi pi-users" style="font-size: 3rem"></i>
                    <h3>Aucun étudiant disponible</h3>
                    <p>
                        Il n'y a pas d'étudiants disponibles dans votre
                        filière.
                    </p>
                </div>
                <div v-else>
                    <div class="search-container mb-3">
                        <span class="p-input-icon-left w-full">
                            <i class="pi pi-search" />
                            <InputText
                                v-model="searchTerm"
                                placeholder="Rechercher un étudiant..."
                                class="w-full"
                            />
                        </span>
                    </div>
                    <DataTable
                        :value="filteredStudents"
                        :loading="loading"
                        responsiveLayout="scroll"
                        stripedRows
                        :paginator="filteredStudents.length > 10"
                        :rows="10"
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                        :rowsPerPageOptions="[10, 20, 50]"
                        emptyMessage="Aucun étudiant disponible"
                    >
                        <Column field="nom" header="Nom" sortable style="min-width: 10rem" />
                        <Column field="prenom" header="Prénom" sortable style="min-width: 10rem" />
                        <Column field="email" header="Email" sortable style="min-width: 15rem" />
                        <Column header="Actions" style="min-width: 8rem; text-align: center">
                            <template #body="slotProps">
                                <Button
                                    icon="pi pi-send"
                                    class="p-button-rounded p-button-outlined p-button-primary"
                                    @click="$emit('send-request', slotProps.data.id)"
                                    :loading="processingStudentId === slotProps.data.id"
                                    :disabled="isProcessingAnyStudent"
                                    tooltip="Envoyer une demande"
                                    tooltipOptions="top"
                                />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </template>
    </Card>
</template>

<script setup>
import { ref, computed } from 'vue';
import Card from 'primevue/card';
import Badge from 'primevue/badge';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import InputText from 'primevue/inputtext';

const props = defineProps({
    availableStudents: {
        type: Array,
        default: () => []
    },
    loading: {
        type: Boolean,
        default: false
    },
    hasSentRequest: {
        type: Boolean,
        default: false
    },
    processingStudentId: {
        type: Number,
        default: null
    },
    processingCancel: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['send-request', 'cancel-request']);

const searchTerm = ref('');

const isProcessingAnyStudent = computed(() => props.processingStudentId !== null);

const filteredStudents = computed(() => {
    if (!searchTerm.value.trim()) return props.availableStudents;
    
    const term = searchTerm.value.toLowerCase().trim();
    return props.availableStudents.filter(student => 
        student.nom.toLowerCase().includes(term) ||
        student.prenom.toLowerCase().includes(term) ||
        (student.email && student.email.toLowerCase().includes(term))
    );
});
</script>

<style scoped>
.table-card {
    margin-bottom: 2rem;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--primary-color);
}

.search-container {
    margin-bottom: 1rem;
}

.w-full {
    width: 100%;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
}

.loading-container p {
    margin-top: 16px;
    color: var(--text-color-secondary);
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 40px 20px;
}

.empty-state i {
    color: var(--primary-color);
    opacity: 0.6;
    margin-bottom: 16px;
}

.empty-state h3 {
    margin: 0 0 8px;
    color: var(--text-color);
}

.empty-state p {
    margin: 0 0 24px;
    color: var(--text-color-secondary);
}

.sent-request-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 30px;
    background-color: var(--surface-hover);
    border-radius: 8px;
}

.sent-request-info i {
    font-size: 2rem;
    color: var(--primary-color);
    margin-bottom: 16px;
}

.sent-request-info h3 {
    margin: 0 0 8px;
}

.sent-request-info p {
    margin: 0;
    color: var(--text-color-secondary);
}

.mt-3 {
    margin-top: 1rem;
}

.mb-3 {
    margin-bottom: 1rem;
}
</style>
