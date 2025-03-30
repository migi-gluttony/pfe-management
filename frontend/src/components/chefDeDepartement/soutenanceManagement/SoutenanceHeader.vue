<template>
    <div class="header-section">
        <div class="title-filter-group">
            <h1 class="page-title">Gestion des Soutenances</h1>
            <Dropdown
                v-model="selectedFiliereModel"
                :options="filieres"
                optionLabel="nom"
                optionValue="id"
                placeholder="Filtrer par filière"
                class="filter-dropdown"
                @change="$emit('change:filiere')"
            />
        </div>
        <div class="action-buttons">
            <!-- No action button in this view, but space is reserved -->
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";
import Dropdown from "primevue/dropdown";

const props = defineProps({
    selectedFiliere: {
        type: [Number, String, null],
        default: null,
    },
    filieres: {
        type: Array,
        default: () => [],
    },
});

const emit = defineEmits(["update:selectedFiliere", "change:filiere"]);

// Two-way binding for selectedFiliere
const selectedFiliereModel = computed({
    get: () => props.selectedFiliere,
    set: (value) => emit("update:selectedFiliere", value),
});
</script>

<style scoped>
/* Header section styling */
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
    align-items: center;
    gap: 1.5rem;
    flex-wrap: wrap;
}

.page-title {
    margin: 0;
    font-size: 2rem;
    font-weight: 600;
    color: var(--primary-color);
    white-space: nowrap;
}

.filter-dropdown {
    font-size: 1.5rem;
}

:deep(.filter-dropdown .p-dropdown-label) {
    display: flex;
    align-items: center;
}

:deep(.filter-dropdown .p-dropdown-trigger) {
    width: 2.5rem;
}

.action-buttons {
    display: flex;
    gap: 0.75rem;
}

/* Responsive styles */
@media (max-width: 768px) {
    .header-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .title-filter-group {
        flex-direction: column;
        align-items: flex-start;
        width: 100%;
        gap: 0.75rem;
    }

    .filter-dropdown {
        width: 100%;
        min-width: 100%;
    }

    .action-buttons {
        width: 100%;
        justify-content: flex-end;
    }
}
</style>
