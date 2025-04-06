<template>
    <Dialog 
        :visible="visible" 
        @update:visible="$emit('update:visible', $event)"
        :modal="true"
        :closable="false"
        header="Proposer un Nouveau Sujet"
        :style="{ width: '600px' }"
        class="suggestion-dialog"
    >
        <div class="form-container p-fluid">
            <div class="field">
                <label for="titre">Titre du sujet<span class="required-field">*</span></label>
                <InputText
                    id="titre"
                    v-model="formData.titre"
                    :class="{ 'p-invalid': formErrors.titre }"
                    placeholder="Titre concis et descriptif du projet"
                />
                <small v-if="formErrors.titre" class="p-error">{{ formErrors.titre }}</small>
            </div>

            <div class="field">
                <label for="theme">Thème du sujet<span class="required-field">*</span></label>
                <InputText
                    id="theme"
                    v-model="formData.theme"
                    :class="{ 'p-invalid': formErrors.theme }"
                    placeholder="Ex: Intelligence Artificielle, Web, Mobile..."
                />
                <small v-if="formErrors.theme" class="p-error">{{ formErrors.theme }}</small>
            </div>

            <div class="field">
                <label for="description">Description détaillée<span class="required-field">*</span></label>
                <Textarea
                    id="description"
                    v-model="formData.description"
                    rows="6"
                    :class="{ 'p-invalid': formErrors.description }"
                    placeholder="Décrivez le but du projet, les objectifs, les technologies envisagées..."
                    autoResize
                />
                <small v-if="formErrors.description" class="p-error">{{ formErrors.description }}</small>
            </div>
        </div>

        <template #footer>
            <Button
                label="Annuler"
                icon="pi pi-times"
                class="p-button-text"
                @click="$emit('cancel')"
                :disabled="submitting"
            />
            <Button
                label="Soumettre"
                icon="pi pi-check"
                class="p-button-primary"
                @click="submitForm"
                :loading="submitting"
                :disabled="submitting"
            />
        </template>
    </Dialog>
</template>

<script setup>
import { reactive, watch } from 'vue';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

const props = defineProps({
    visible: {
        type: Boolean,
        required: true
    },
    submitting: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:visible', 'submit', 'cancel']);

// Form data and errors
const formData = reactive({
    titre: '',
    theme: '',
    description: ''
});

const formErrors = reactive({
    titre: '',
    theme: '',
    description: ''
});

// Reset form when modal opens/closes
watch(() => props.visible, (newValue) => {
    if (newValue) {
        // Reset form when opening
        resetForm();
    }
});

// Validate form
const validateForm = () => {
    let isValid = true;

    // Reset errors
    formErrors.titre = '';
    formErrors.theme = '';
    formErrors.description = '';

    // Validate titre
    if (!formData.titre.trim()) {
        formErrors.titre = "Le titre est obligatoire";
        isValid = false;
    } else if (formData.titre.length < 5) {
        formErrors.titre = "Le titre doit contenir au moins 5 caractères";
        isValid = false;
    }

    // Validate theme
    if (!formData.theme.trim()) {
        formErrors.theme = "Le thème est obligatoire";
        isValid = false;
    }

    // Validate description
    if (!formData.description.trim()) {
        formErrors.description = "La description est obligatoire";
        isValid = false;
    } else if (formData.description.length < 50) {
        formErrors.description = "La description doit contenir au moins 50 caractères";
        isValid = false;
    }

    return isValid;
};

// Submit form
const submitForm = () => {
    if (validateForm()) {
        emit('submit', { ...formData });
    }
};

// Reset form
const resetForm = () => {
    formData.titre = '';
    formData.theme = '';
    formData.description = '';
    
    formErrors.titre = '';
    formErrors.theme = '';
    formErrors.description = '';
};
</script>

<style scoped>
.form-container {
    padding: 1rem 0;
}

.field {
    margin-bottom: 1.5rem;
}

.field label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required-field {
    color: var(--red-500);
    margin-left: 0.25rem;
}

.p-fluid .p-inputtext,
.p-fluid .p-textarea {
    width: 100%;
}
</style>
