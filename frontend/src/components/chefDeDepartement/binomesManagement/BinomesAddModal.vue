<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Ajouter un Nouveau Binôme"
        :modal="true"
        class="binome-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div class="field-container">
            <div class="p-field">
                <label for="etudiant1">Étudiant 1 <span class="required">*</span></label>
                <Dropdown
                    id="etudiant1"
                    v-model="formData.etudiant1Id"
                    :options="processedStudents"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un étudiant"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                    @change="handleStudent1Change"
                />
            </div>
            
            <div class="p-field">
                <label for="etudiant2">Étudiant 2</label>
                <Dropdown
                    id="etudiant2"
                    v-model="formData.etudiant2Id"
                    :options="filteredStudent2Options"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un étudiant (optionnel)"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    :disabled="!formData.etudiant1Id"
                />
            </div>
            
            <div class="p-field">
                <label for="encadrant">Encadrant <span class="required">*</span></label>
                <Dropdown
                    id="encadrant"
                    v-model="formData.encadrantId"
                    :options="processedEncadrants"
                    optionLabel="fullName"
                    optionValue="id"
                    placeholder="Sélectionner un encadrant"
                    class="w-full"
                    :filter="true"
                    filterMatchMode="contains"
                    :showClear="true"
                    :virtualScrollerOptions="{ itemSize: 38 }"
                    required
                />
            </div>
            
            <div class="p-field">
                <label for="sujet">Sujet <span class="required">*</span></label>
                <Dropdown
                    id="sujet"
                    v-model="formData.sujetId"
                    :options="availableSujets"
                    optionLabel="titre"
                    optionValue="id"
                    placeholder="Sélectionner un sujet"
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
                label="Ajouter"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    selectedFiliere: {
        type: Number,
        required: true
    },
    availableStudents: {
        type: Array,
        default: () => [],
    },
    encadrants: {
        type: Array,
        default: () => [],
    },
    availableSujets: {
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
const formData = ref({
    etudiant1Id: null,
    etudiant2Id: null,
    encadrantId: null,
    sujetId: null,
});


// Filter students by filiere in a computed property
const filteredStudents = computed(() => {
    return props.availableStudents.filter(student => 
        student.filiereId === props.selectedFiliere
    );
});


const processedEncadrants = computed(() => {
    return props.encadrants.map(encadrant => ({
        ...encadrant,
        fullName: `${encadrant.nom} ${encadrant.prenom}`
    }));
});

// Filtered options for student 2 - depends on student 1 selection
const filteredStudent2Options = computed(() => {
    if (!formData.value.etudiant1Id) return [];
    return processedStudents.value.filter(s => s.id !== formData.value.etudiant1Id);
});

// Reset form when modal opens or closes
watch(() => props.visible, (newValue) => {
    if (newValue) {
        resetForm();
    }
});

// Methods
function handleStudent1Change() {
    // Clear student2 when student1 changes
    formData.value.etudiant2Id = null;
}

function resetForm() {
    formData.value = {
        etudiant1Id: null,
        etudiant2Id: null,
        encadrantId: null,
        sujetId: null,
    };
}

function submitForm() {
    // Create a new object to avoid reactivity issues
    const formDataCopy = { ...formData.value };
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

:deep(.p-dropdown) {
    width: 100%;
}
</style>