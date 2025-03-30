<template>
    <div class="header-section no-print">
        <div class="title-filter-group">
            <h1 class="page-title">Gestion des Notes</h1>
            <Dropdown
                v-model="selectedFiliereModel"
                :options="filieres"
                optionLabel="nom"
                optionValue="id"
                placeholder="Filtrer par filière"
                class="filter-dropdown"
                @change="$emit('filiereChange', selectedFiliereModel)"
            />
        </div>
        <div class="action-buttons">
            <Button
                label="Imprimer"
                icon="pi pi-print"
                class="p-button-primary action-btn"
                @click="$emit('print')"
            />
        </div>
    </div>
</template>

<script setup>
import { computed } from "vue";
import Dropdown from "primevue/dropdown";
import Button from "primevue/button";

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

const emit = defineEmits(["update:selectedFiliere", "filiereChange", "print"]);

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

    .action-btn {
        width: 100%;
    }
}
</style>
