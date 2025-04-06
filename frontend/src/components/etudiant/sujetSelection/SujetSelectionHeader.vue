<template>
    <div class="header-section">
        <div class="title-filter-group">
            <h1 class="page-title">Sélection de Sujet</h1>
            <p class="page-subtitle">Choisissez un sujet pour votre projet de fin d'études</p>
        </div>
        <div class="action-buttons">
            <Button
                label="Proposer un sujet"
                icon="pi pi-plus"
                class="p-button-primary action-btn"
                @click="$emit('open-suggestion')"
                :disabled="disabled"
            />
        </div>
    </div>
</template>

<script setup>
import Button from "primevue/button";

const props = defineProps({
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['open-suggestion']);
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
