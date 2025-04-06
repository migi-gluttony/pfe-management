<template>
    <Card v-if="showSujetList" class="table-card">
        <template #title>
            <div class="section-header">
                <h3>Sujets disponibles</h3>
                <div class="search-container">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText
                            v-model="searchTerm"
                            placeholder="Rechercher un sujet..."
                        />
                    </span>
                </div>
            </div>
        </template>
        <template #content>
            <div
                v-if="filteredSujets.length === 0"
                class="empty-state"
            >
                <i class="pi pi-file-o empty-icon"></i>
                <h3>Aucun sujet disponible</h3>
                <p>
                    Aucun sujet n'est disponible pour votre
                    filière actuellement.
                </p>
            </div>

            <div v-else class="sujet-cards">
                <Card
                    v-for="sujet in filteredSujets"
                    :key="sujet.id"
                    class="sujet-card"
                >
                    <template #header>
                        <div class="sujet-card-header">
                            <h4>{{ sujet.titre }}</h4>
                        </div>
                    </template>
                    <template #content>
                        <div class="sujet-tags">
                            <Tag
                                severity="info"
                                :value="sujet.theme"
                            />
                            <Tag
                                severity="secondary"
                                :value="sujet.filiereName"
                            />
                        </div>

                        <div class="description-preview">
                            <p>
                                {{ truncateDescription(sujet.description) }}
                            </p>
                        </div>

                        <Button
                            label="Sélectionner ce sujet"
                            icon="pi pi-check"
                            class="p-button-primary mt-3 w-full"
                            @click="$emit('select-sujet', sujet)"
                            :loading="processingSubjectId === sujet.id"
                            :disabled="isProcessingAnyAction"
                        />
                    </template>
                </Card>
            </div>
        </template>
    </Card>
</template>

<script setup>
import { ref, computed } from 'vue';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';

const props = defineProps({
    sujets: {
        type: Array,
        default: () => []
    },
    showSujetList: {
        type: Boolean,
        default: false
    },
    processingSubjectId: {
        type: Number,
        default: null
    },
    isProcessingAnyAction: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['select-sujet']);

const searchTerm = ref('');

// Filter sujets based on search term
const filteredSujets = computed(() => {
    if (!searchTerm.value) return props.sujets;

    const term = searchTerm.value.toLowerCase();
    return props.sujets.filter(
        (sujet) =>
            sujet.titre.toLowerCase().includes(term) ||
            sujet.theme.toLowerCase().includes(term) ||
            sujet.description.toLowerCase().includes(term)
    );
});

// Truncate description for preview
const truncateDescription = (text, length = 150) => {
    if (!text) return "";
    if (text.length <= length) return text;
    return text.substring(0, length) + "...";
};
</script>

<style scoped>
.table-card {
    margin-bottom: 2rem;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.section-header h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--primary-color);
    font-weight: 600;
}

.search-container {
    min-width: 250px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 3rem 0;
    background-color: var(--surface-hover);
    border-radius: 8px;
}

.empty-state .empty-icon {
    font-size: 3rem;
    color: var(--text-color-secondary);
    margin-bottom: 1rem;
}

.empty-state h3 {
    margin: 0 0 0.5rem;
    color: var(--text-color);
    font-size: 1.25rem;
}

.empty-state p {
    margin: 0;
    color: var(--text-color-secondary);
}

/* Sujet cards */
.sujet-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
}

.sujet-card {
    transition: transform 0.2s, box-shadow 0.2s;
}

.sujet-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sujet-card-header {
    padding: 1rem;
    background-color: var(--primary-color);
    color: white;
}

.sujet-card-header h4 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 500;
}

.sujet-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.description-preview {
    margin: 1rem 0;
    color: var(--text-color);
    line-height: 1.5;
}

.mt-3 {
    margin-top: 0.75rem;
}

.w-full {
    width: 100%;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.75rem;
    }

    .search-container {
        width: 100%;
    }

    .sujet-cards {
        grid-template-columns: 1fr;
    }
}
</style>
