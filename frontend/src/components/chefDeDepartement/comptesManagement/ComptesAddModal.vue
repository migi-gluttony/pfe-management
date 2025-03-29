<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        header="Ajouter un Nouveau Compte"
        :modal="true"
        class="compte-dialog"
        :style="{ width: '600px' }"
        :closable="false"
    >
        <div class="field-container">
            <div class="p-field">
                <label for="role">Rôle <span class="required">*</span></label>
                <Dropdown
                    id="role"
                    v-model="formData.role"
                    :options="roleOptions"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Sélectionner un rôle"
                    class="w-full"
                    @change="handleRoleChange"
                    required
                />
            </div>

            <div class="p-field">
                <label for="nom">Nom <span class="required">*</span></label>
                <InputText
                    id="nom"
                    v-model="formData.nom"
                    class="w-full"
                    placeholder="Nom de famille"
                    required
                />
            </div>

            <div class="p-field">
                <label for="prenom"
                    >Prénom <span class="required">*</span></label
                >
                <InputText
                    id="prenom"
                    v-model="formData.prenom"
                    class="w-full"
                    placeholder="Prénom"
                    required
                />
            </div>

            <div v-if="formData.role === 'ETUDIANT'" class="p-field">
                <label for="cne">CNE <span class="required">*</span></label>
                <InputText
                    id="cne"
                    v-model="formData.cne"
                    class="w-full"
                    placeholder="CNE"
                    required
                />
            </div>

            <div v-if="formData.role !== 'ETUDIANT'" class="p-field">
                <label for="cni">CNI <span class="required">*</span></label>
                <InputText
                    id="cni"
                    v-model="formData.cni"
                    class="w-full"
                    placeholder="CNI"
                    required
                />
            </div>

            <div class="p-field">
                <label for="dateNaissance"
                    >Date de naissance <span class="required">*</span></label
                >
                <Calendar
                    id="dateNaissance"
                    v-model="formData.dateNaissance"
                    :showIcon="true"
                    dateFormat="dd/mm/yy"
                    class="w-full"
                    placeholder="JJ/MM/AAAA"
                    required
                />
            </div>

            <div v-if="formData.role === 'ETUDIANT'" class="p-field">
                <label for="filiereId"
                    >Filière <span class="required">*</span></label
                >
                <Dropdown
                    id="filiereId"
                    v-model="formData.filiereId"
                    :options="filieres"
                    optionLabel="nom"
                    optionValue="id"
                    placeholder="Sélectionner une filière"
                    class="w-full"
                    required
                />
            </div>

            <div class="info-box">
                <i class="pi pi-info-circle"></i>
                <p>
                    L'email sera généré automatiquement au format:
                    prenomnom.efb@usms.ac.ma
                </p>
                <p>
                    Le mot de passe sera généré aléatoirement et affiché dans la
                    console.
                </p>
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
import { reactive, watch } from "vue";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import InputText from "primevue/inputtext";
import Calendar from "primevue/calendar";
import Button from "primevue/button";

const props = defineProps({
    visible: {
        type: Boolean,
        required: true,
    },
    filieres: {
        type: Array,
        default: () => [],
    },
    submitting: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(["update:visible", "submit", "cancel"]);

// Role options
const roleOptions = [
    { label: "Étudiant", value: "ETUDIANT" },
    { label: "Encadrant", value: "ENCADRANT" },
    { label: "Jury", value: "JURY" },
];

// Form data
const formData = reactive({
    nom: "",
    prenom: "",
    cni: "",
    cne: "",
    dateNaissance: new Date(),
    role: "ETUDIANT",
    filiereId: null,
});

// Reset form when modal opens
watch(
    () => props.visible,
    (newValue) => {
        if (newValue) {
            resetForm();
        }
    }
);

function handleRoleChange() {
    // Reset fields specific to role
    if (formData.role === "ETUDIANT") {
        formData.cni = "";
    } else {
        formData.cne = "";
        formData.filiereId = null;
    }
}

function resetForm() {
    Object.assign(formData, {
        nom: "",
        prenom: "",
        cni: "",
        cne: "",
        dateNaissance: new Date(),
        role: "ETUDIANT",
        filiereId: null,
    });
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

.info-box {
    background-color: var(--surface-hover);
    border-radius: 6px;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    margin-top: 1rem;
}

.info-box i {
    color: var(--primary-color);
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
}

.info-box p {
    margin: 0.25rem 0;
    font-size: 0.9rem;
}

:deep(.p-calendar) {
    width: 100%;
}

:deep(.p-dropdown) {
    width: 100%;
}
</style>
