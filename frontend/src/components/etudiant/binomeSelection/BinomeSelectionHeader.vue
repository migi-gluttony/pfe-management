<template>
    <div class="header-section">
        <div class="title-filter-group">
            <h1 class="page-title">Sélection de Binôme</h1>
            <p class="page-subtitle">Choisissez un étudiant pour former votre binôme</p>
        </div>
        <div class="action-buttons">
            <Button
                v-if="!hasSentRequest"
                label="Continuer Seul"
                icon="pi pi-user"
                class="p-button-primary action-btn"
                @click="$emit('continue-alone')"
                :loading="processingAlone"
                :disabled="disabled"
            />
        </div>
    </div>
</template>

<script setup>
import Button from "primevue/button";

const props = defineProps({
    hasSentRequest: {
        type: Boolean,
        default: false
    },
    processingAlone: {
        type: Boolean,
        default: false
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['continue-alone']);
</script>

<style scoped>
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
}

.dark-mode .header-section {
    border-bottom-color: #333;
}

.title-filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.page-title {
    margin: 0;
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    white-space: nowrap;
}

.page-subtitle {
    color: var(--text-color-secondary);
    margin: 0;
}

.action-buttons {
    display: flex;
    gap: 0.75rem;
}

.action-btn {
    height: 38px;
    min-width: 150px;
    font-weight: 500;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    transition: transform 0.2s, box-shadow 0.2s;
}

.action-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Responsive styles */
@media (max-width: 768px) {
    .header-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .title-filter-group {
        width: 100%;
    }

    .action-buttons {
        width: 100%;
    }

    .action-btn {
        width: 100%;
    }
}
</style>
