<template>
    <Card class="options-card">
        <template #title>
            <div class="card-header">
                <h3>Méthodes de Sélection</h3>
            </div>
        </template>
        <template #content>
            <div class="option-cards">
                <!-- Option 1: Choose from list -->
                <div
                    class="option-card"
                    @click="$emit('toggle-sujet-list')"
                    :class="{ 'option-active': showSujetList }"
                >
                    <div class="option-icon bg-primary">
                        <i class="pi pi-list"></i>
                    </div>
                    <div class="option-content">
                        <h4>Sélection manuelle</h4>
                        <p>
                            Choisissez un sujet parmi la liste des
                            propositions disponibles
                        </p>
                    </div>
                </div>

                <!-- Option 2: Random assignment -->
                <div
                    class="option-card"
                    @click="$emit('random-assignment')"
                    :class="{ 'option-disabled': isProcessing }"
                >
                    <div class="option-icon bg-danger">
                        <i class="pi pi-sync"></i>
                    </div>
                    <div class="option-content">
                        <h4>Attribution aléatoire</h4>
                        <p>
                            Un sujet vous sera attribué au hasard
                            parmi ceux disponibles
                        </p>
                    </div>
                </div>
            </div>
        </template>
    </Card>
</template>

<script setup>
import Card from 'primevue/card';

const props = defineProps({
    showSujetList: {
        type: Boolean,
        default: false
    },
    isProcessing: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['toggle-sujet-list', 'random-assignment']);
</script>

<style scoped>
.options-card {
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

/* Option cards */
.option-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
}

.option-card {
    display: flex;
    align-items: center;
    padding: 1.5rem;
    background-color: var(--surface-card);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 2px solid transparent;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.option-card:hover:not(.option-disabled) {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.option-active {
    border-color: var(--primary-color);
    background-color: rgba(var(--primary-color-rgb), 0.05);
}

.option-disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.option-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    margin-right: 1rem;
    color: white;
}

.bg-primary {
    background-color: var(--primary-color);
}

.bg-danger {
    background-color: var(--red-500);
}

.option-content h4 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
    color: var(--text-color);
}

.option-content p {
    margin: 0;
    color: var(--text-color-secondary);
    font-size: 0.9rem;
}

/* Responsive styles */
@media (max-width: 768px) {
    .option-cards {
        grid-template-columns: 1fr;
    }
}
</style>
