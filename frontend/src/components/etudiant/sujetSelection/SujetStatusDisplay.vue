<template>
    <!-- Has pending suggestion -->
    <Card v-if="hasPendingSuggestion" class="status-card">
        <template #content>
            <div class="status-container pending-container">
                <div class="status-icon">
                    <i class="pi pi-clock"></i>
                </div>
                <div class="status-content">
                    <h3>Suggestion en attente d'approbation</h3>
                    <p>
                        Votre suggestion de sujet est en cours
                        d'évaluation par le chef de département.
                    </p>
                    <Tag severity="warning" value="En attente" />
                </div>
            </div>
        </template>
    </Card>

    <!-- Already has selected sujet -->
    <Card v-else-if="hasSujet" class="status-card">
        <template #content>
            <div class="status-container selected-container">
                <div class="status-icon">
                    <i class="pi pi-check-circle"></i>
                </div>
                <div class="status-content">
                    <h3>Sujet sélectionné avec succès</h3>

                    <div
                        v-if="selectedSujet"
                        class="selected-sujet-details"
                    >
                        <h4>{{ selectedSujet.titre }}</h4>

                        <div class="sujet-tags">
                            <Tag
                                severity="info"
                                :value="selectedSujet.theme"
                            />
                            <Tag
                                severity="secondary"
                                :value="selectedSujet.filiereName"
                            />
                        </div>

                        <div class="description-section">
                            <h5>Description</h5>
                            <p>{{ selectedSujet.description }}</p>
                        </div>
                    </div>

                    <Button
                        label="Retour au tableau de bord"
                        icon="pi pi-home"
                        class="mt-3"
                        @click="$emit('go-to-dashboard')"
                    />
                </div>
            </div>
        </template>
    </Card>
</template>

<script setup>
import Card from 'primevue/card';
import Tag from 'primevue/tag';
import Button from 'primevue/button';

const props = defineProps({
    hasPendingSuggestion: {
        type: Boolean,
        default: false
    },
    hasSujet: {
        type: Boolean,
        default: false
    },
    selectedSujet: {
        type: Object,
        default: null
    }
});

const emit = defineEmits(['go-to-dashboard']);
</script>

<style scoped>
.status-card {
    margin-bottom: 2rem;
}

/* Status containers */
.status-container {
    display: flex;
    align-items: flex-start;
    padding: 1.5rem;
    border-radius: 8px;
}

.pending-container {
    background-color: var(--yellow-50);
    border-left: 4px solid var(--yellow-500);
}

.selected-container {
    background-color: var(--green-50);
    border-left: 4px solid var(--green-500);
}

.status-icon {
    font-size: 2rem;
    margin-right: 1rem;
    color: var(--primary-color);
}

.pending-container .status-icon {
    color: var(--yellow-500);
}

.selected-container .status-icon {
    color: var(--green-500);
}

.status-content {
    flex: 1;
}

.status-content h3 {
    margin: 0 0 0.75rem;
    font-size: 1.25rem;
    color: var(--text-color);
}

.status-content p {
    margin: 0 0 1rem;
    color: var(--text-color-secondary);
}

/* Selected sujet details */
.selected-sujet-details {
    background-color: var(--surface-card);
    border-radius: 8px;
    padding: 1.5rem;
    margin: 1rem 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.selected-sujet-details h4 {
    margin: 0 0 1rem;
    font-size: 1.2rem;
    color: var(--primary-color);
}

.sujet-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.description-section h5 {
    margin: 0 0 0.5rem;
    font-size: 1rem;
    color: var(--text-color);
}

.description-section p {
    margin: 0;
    line-height: 1.5;
}

.mt-3 {
    margin-top: 0.75rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .status-container {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .status-icon {
        margin-right: 0;
        margin-bottom: 1rem;
    }
}
</style>
