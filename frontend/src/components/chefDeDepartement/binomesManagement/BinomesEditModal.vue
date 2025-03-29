<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Changer l'Encadrant du Binôme"
        :modal="true"
        class="binome-dialog"
        :style="{ width: '500px' }"
        :closable="false"
    >
        <div v-if="binome" class="field-container">
            <div class="current-info">
                <p><strong>Binôme:</strong> {{ getStudentFullName(binome.etudiant1) }}
                    {{ binome.etudiant2 ? '& ' + getStudentFullName(binome.etudiant2) : '' }}</p>
                <p><strong>Encadrant actuel:</strong> {{ getEncadrantFullName(binome.encadrant) }}</p>
            </div>
            
            <div class="p-field">
                <label for="new-encadrant">Nouvel Encadrant <span class="required">*</span></label>
                <Dropdown
                    id="new-encadrant"
                    v-model="formData.encadrantId"
                    :options="processedEncadrants"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un encadrant"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                />
            </div>
        </div>
        
        <template #footer>
            <Button
                label="Annuler"
                icon="pi pi-times"
                class="p-button-text"
                @click="$emit('cancel')"
            />
            <Button
                label="Sauvegarder"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { reactive, computed, watch } from "vue";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    binome: {
        type: Object,
        default: null,
    },
    encadrants: {
        type: Array,
        default: () => [],
    },
    submitting: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(["update:visible", "submit", "cancel"]);

// Form data
const formData = reactive({
    encadrantId: null,
});

// Process encadrants for dropdown
const processedEncadrants = computed(() => {
    return props.encadrants.map(encadrant => ({
        ...encadrant,
        fullName: `${encadrant.nom} ${encadrant.prenom}`
    }));
});

// Watch for changes in the binome prop to update the form
watch(
    () => props.binome,
    (newValue) => {
        if (newValue && newValue.encadrant) {
            formData.encadrantId = newValue.encadrant.id;
        }
    },
    { immediate: true }
);

// Helper methods
function getStudentFullName(student) {
    if (!student) return '-';
    return `${student.nom} ${student.prenom}`;
}

function getEncadrantFullName(encadrant) {
    if (!encadrant) return '-';
    return `${encadrant.nom} ${encadrant.prenom}`;
}

function submitForm() {
    // Create a new object to avoid reactivity issues
    const formDataCopy = { ...formData };
    emit("submit", formDataCopy);
}
</script>

<style scoped>
.field-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.p-field {
    margin-bottom: 0;
}

.p-field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required {
    color: red;
}

.current-info {
    background-color: var(--surface-ground);
    padding: 1rem;
    border-radius: 6px;
    margin-bottom: 1rem;
}

.current-info p {
    margin: 0.5rem 0;
}

:deep(.p-dropdown) {
    width: 100%;
}
</style>