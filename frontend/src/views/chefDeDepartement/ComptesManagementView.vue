<template>
    <div class="comptes-management">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un compte..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <!-- Comptes Header -->
        <ComptesHeader
            :selectedRole="selectedRole"
            @role-change="handleRoleChange"
            @add="openAddModal"
            @import="openImportModal"
        />

        <!-- Comptes List -->
        <ComptesList
            :comptes="comptes"
            :loading="loading"
            :selectedRole="selectedRole"
            :searchQuery="searchQuery"
            @edit="openEditModal"
            @delete="confirmDelete"
        />

        <!-- Add Compte Modal -->
        <ComptesAddModal
            v-model:visible="showAddModal"
            :filieres="filieres"
            :submitting="submitting"
            @submit="addCompte"
            @cancel="closeModals"
        />

        <!-- Edit Compte Modal -->
        <ComptesEditModal
            v-model:visible="showEditModal"
            :compte="editingCompte"
            :filieres="filieres"
            :submitting="submitting"
            @submit="saveEditedCompte"
            @cancel="closeModals"
        />

        <!-- Import Users Modal (using original component) -->
        <ComptesImport
            v-model:visible="showImportModal"
            :role="selectedRole"
            :filieres="filieres"
            @import-users="importUsers"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";

// Import components
import UserInfoHeader from "@/components/UserInfoHeader.vue";
import ComptesHeader from "@/components/chefDeDepartement/comptesManagement/ComptesHeader.vue";
import ComptesList from "@/components/chefDeDepartement/comptesManagement/ComptesList.vue";
import ComptesAddModal from "@/components/chefDeDepartement/comptesManagement/ComptesAddModal.vue";
import ComptesEditModal from "@/components/chefDeDepartement/comptesManagement/ComptesEditModal.vue";
import ComptesImport from "@/components/chefDeDepartement/comptesManagement/ComptesImport.vue";

// Import PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Component state
const comptes = ref([]);
const filieres = ref([]);
const selectedRole = ref("ETUDIANT");
const editingCompte = ref(null);
const searchQuery = ref("");

// UI state
const showAddModal = ref(false);
const showEditModal = ref(false);
const showImportModal = ref(false);
const loading = ref(false);
const submitting = ref(false);

// Services
const toast = useToast();
const confirm = useConfirm();

// Fetch data on component mount
onMounted(async () => {
    await fetchData(selectedRole.value);
});

// Methods for fetching data
async function fetchData(role = "ETUDIANT") {
    loading.value = true;
    try {
        const endpoint = `/chef_de_departement/comptes?role=${role}`;
        const response = await ApiService.get(endpoint);

        if (response && response.comptes) {
            comptes.value = response.comptes;
        }

        if (response && response.filieres) {
            filieres.value = response.filieres;
        }
    } catch (error) {
        handleApiError(error, "Erreur lors du chargement des données");
    } finally {
        loading.value = false;
    }
}

function handleRoleChange(role) {
    selectedRole.value = role;
    fetchData(role);
}

// CRUD operations
async function addCompte(compteData) {
    submitting.value = true;
    try {
        const response = await ApiService.post(
            "/chef_de_departement/comptes",
            compteData
        );

        if (response) {
            comptes.value.push(response);
            showAddModal.value = false;

            toast.add({
                severity: "success",
                summary: "Compte ajouté",
                detail: "Le compte a été ajouté avec succès",
                life: 3000,
            });
        }
    } catch (error) {
        handleApiError(error, "Erreur lors de l'ajout du compte");
    } finally {
        submitting.value = false;
    }
}

async function saveEditedCompte(editData) {
    if (!editingCompte.value) return;

    submitting.value = true;
    try {
        const response = await ApiService.put(
            `/chef_de_departement/comptes/${editingCompte.value.id}`,
            editData
        );

        if (response) {
            // Update local data
            const index = comptes.value.findIndex(
                (c) => c.id === editingCompte.value.id
            );
            if (index !== -1) {
                comptes.value[index] = response;
            }

            showEditModal.value = false;

            toast.add({
                severity: "success",
                summary: "Compte mis à jour",
                detail: "Le compte a été modifié avec succès",
                life: 3000,
            });
        }
    } catch (error) {
        handleApiError(error, "Erreur lors de la modification du compte");
    } finally {
        submitting.value = false;
    }
}

function confirmDelete(compte) {
    confirm.require({
        message: `Êtes-vous sûr de vouloir supprimer le compte de ${compte.prenom} ${compte.nom}?`,
        header: "Confirmation de suppression",
        icon: "pi pi-exclamation-triangle",
        acceptClass: "p-button-danger",
        accept: () => deleteCompte(compte),
        reject: () => {
            /* do nothing */
        },
    });
}


async function deleteCompte(compte) {
    loading.value = true;
    try {
        await ApiService.delete(`/chef_de_departement/comptes/${compte.id}`);

        // Update local data
        comptes.value = comptes.value.filter((c) => c.id !== compte.id);

        toast.add({
            severity: "success",
            summary: "Compte supprimé",
            detail: "Le compte a été supprimé avec succès",
            life: 3000,
        });
    } catch (error) {
        // Check for foreign key constraint error
        if (
            error.response?.status === 500 &&
            (error.response?.data?.includes("constraint") ||
                error.response?.data?.message?.includes("constraint"))
        ) {
            toast.add({
                severity: "error",
                summary: "Impossible de supprimer le compte",
                detail: "Ce compte est associé à d'autres données du système (notes, binômes, etc.). Veuillez d'abord supprimer ces associations.",
                life: 10000,
            });
        } else {
            handleApiError(error, "Erreur lors de la suppression du compte");
        }
    } finally {
        loading.value = false;
    }
}

// UI control methods
function openAddModal() {
    showAddModal.value = true;
}

function openImportModal() {
    showImportModal.value = true;
}

function openEditModal(compte) {
    editingCompte.value = { ...compte };
    showEditModal.value = true;
}

function closeModals() {
    showAddModal.value = false;
    showEditModal.value = false;
    showImportModal.value = false;
    editingCompte.value = null;
}

// Import users
async function importUsers(users) {
    if (!users || !Array.isArray(users) || users.length === 0) {
        toast.add({
            severity: "warn",
            summary: "Importation vide",
            detail: "Aucun utilisateur à importer",
            life: 3000,
        });
        return;
    }

    loading.value = true;

    try {
        // Create import request with role set for each user
        const userImports = users.map((user) => ({
            ...user,
            role: selectedRole.value,
        }));

        // Use the batch import endpoint
        const response = await ApiService.post(
            "/chef_de_departement/comptes/import",
            {
                comptes: userImports,
            }
        );

        // Refresh data after import
        await fetchData(selectedRole.value);

        // Show success message
        if (response) {
            const {
                successCount = 0,
                failedCount = 0,
                results = [],
            } = response;

            toast.add({
                severity: "success",
                summary: "Importation terminée",
                detail: `${successCount} compte(s) importé(s) avec succès${
                    failedCount > 0 ? `, ${failedCount} échec(s)` : ""
                }`,
                life: 5000,
            });

            // If there are failures, show details
            if (failedCount > 0 && Array.isArray(results)) {
                const failedItems = results.filter((item) => !item.success);
                failedItems.forEach((item) => {
                    toast.add({
                        severity: "warn",
                        summary: "Échec d'importation",
                        detail: item.message || "Erreur inconnue",
                        life: 7000,
                    });
                });
            }
        }
    } catch (error) {
        handleApiError(error, "Erreur lors de l'importation des comptes");
    } finally {
        loading.value = false;
    }
}

function handleHeaderSearch(query) {
    searchQuery.value = query || "";
}

// Error handling
function handleApiError(error, defaultMessage) {
    console.error(defaultMessage, error);

    let errorMessage = defaultMessage;

    if (error) {
        if (error.response) {
            if (error.response.data) {
                if (typeof error.response.data === "string") {
                    errorMessage = error.response.data;
                } else if (error.response.data.message) {
                    errorMessage = error.response.data.message;
                } else if (error.response.data.error) {
                    errorMessage = error.response.data.error;
                }
            } else if (error.response.status) {
                errorMessage = `${defaultMessage} (${error.response.status})`;
            }
        } else if (error.message) {
            errorMessage = error.message;
        }
    }

    toast.add({
        severity: "error",
        summary: "Erreur",
        detail: errorMessage,
        life: 5000,
    });
}
</script>

<style scoped>
.comptes-management {
    margin: 0 auto;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Dark mode adaptations */
.dark-mode .filter-dropdown {
    background-color: #222;
    border-color: #444;
}

.dark-mode .filter-dropdown :deep(.p-dropdown-label) {
    color: #f0f0f0;
}

.dark-mode .filter-dropdown :deep(.p-dropdown-trigger-icon) {
    color: #aaa;
}
</style>
