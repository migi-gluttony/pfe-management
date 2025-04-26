<template>
    <div class="etudiant-view">
        <Toast />
        <ConfirmDialog />

        <!-- User Info Header -->
        <UserInfoHeader
            searchPlaceholder="Rechercher un étudiant..."
            :initialSearchValue="searchQuery"
            @search="handleHeaderSearch"
        />

        <div class="view-container">
            <!-- Student has pending requests view -->
            <div v-if="hasPendingRequests">
                <BinomeRequestsList
                    :pendingRequests="pendingRequests"
                    :loading="loading"
                    :processingRequestId="processingRequestId"
                    :processingAction="processingAction"
                    @accept="acceptRequest"
                    @reject="rejectRequest"
                    @go-to-choose="goToChooseBinome"
                />
            </div>

            <!-- Choose binome view -->
            <div v-else-if="showChooseBinome">
                <BinomeSelectionHeader
                    :hasSentRequest="hasSentRequest"
                    :processingAlone="processingAlone"
                    :disabled="isProcessingAnyStudent"
                    @continue-alone="continueAlone"
                />

                <AvailableStudentsList
                    :availableStudents="filteredStudents"
                    :loading="loading"
                    :hasSentRequest="hasSentRequest"
                    :processingStudentId="processingStudentId"
                    :processingCancel="processingCancel"
                    @send-request="sendRequest"
                    @cancel-request="cancelRequest"
                />
            </div>
        </div>
    </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import ApiService from "@/services/ApiService";
import UserInfoHeader from "@/components/UserInfoHeader.vue";

// PrimeVue components
import Toast from "primevue/toast";
import ConfirmDialog from "primevue/confirmdialog";

// Custom components
import BinomeSelectionHeader from "@/components/etudiant/binomeSelection/BinomeSelectionHeader.vue";
import BinomeRequestsList from "@/components/etudiant/binomeSelection/BinomeRequestsList.vue";
import AvailableStudentsList from "@/components/etudiant/binomeSelection/AvailableStudentsList.vue";

export default {
    name: "BinomeSelectionView",
    components: {
        Toast,
        ConfirmDialog,
        UserInfoHeader,
        BinomeSelectionHeader,
        BinomeRequestsList,
        AvailableStudentsList,
    },
    setup() {
        // Router and toast
        const router = useRouter();
        const toast = useToast();
        const confirm = useConfirm();

        // State management
        const loading = ref(false);
        const hasPendingRequests = ref(false);
        const pendingRequests = ref([]);
        const showChooseBinome = ref(false);
        const availableStudents = ref([]);
        const hasSentRequest = ref(false);
        const targetStudentId = ref(null);
        const hasBinome = ref(false);
        const searchQuery = ref("");

        // Processing states
        const processingRequestId = ref(null);
        const processingStudentId = ref(null);
        const processingAction = ref(null);
        const processingAlone = ref(false);
        const processingCancel = ref(false);

        // Computed properties
        const isProcessingAnyRequest = computed(
            () => processingRequestId.value !== null
        );
        const isProcessingAnyStudent = computed(
            () => processingStudentId.value !== null
        );
        const filteredStudents = computed(() => {
            if (!searchQuery.value) return availableStudents.value;

            const query = searchQuery.value.toLowerCase();
            return availableStudents.value.filter((student) => {
                return (
                    student.prenom.toLowerCase().includes(query) ||
                    student.nom.toLowerCase().includes(query) ||
                    `${student.prenom} ${student.nom}`
                        .toLowerCase()
                        .includes(query)
                );
            });
        });

        // Check student status on mount
        onMounted(async () => {
            await checkStudentStatus();
        });

        // Check student's current status
        const checkStudentStatus = async () => {
            loading.value = true;
            try {
                const status = await ApiService.get("/etudiant/status");

                // Check if student already has a binome
                if (status.hasBinome) {
                    hasBinome.value = true;
                        router.push("/etudiant/sujet");
                    return;
                }

                // Check for pending requests received
                if (status.hasPendingBinomeRequests) {
                    hasPendingRequests.value = true;
                    await loadPendingRequests();
                } else {
                    // No pending requests, show choose binome view
                    hasPendingRequests.value = false;
                    showChooseBinome.value = true;

                    // Check if student already sent a request
                    hasSentRequest.value = status.hasSentBinomeRequest;

                    // Load available students if no request sent
                    if (!hasSentRequest.value) {
                        await loadAvailableStudents();
                    }
                }
            } catch (error) {
                handleError(error, "Erreur lors de la vérification du statut");
            } finally {
                loading.value = false;
            }
        };

        // Load pending requests received by the student
        const loadPendingRequests = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/etudiant/binome/requests"
                );
                pendingRequests.value = response;
            } catch (error) {
                handleError(error, "Erreur lors du chargement des demandes");
            } finally {
                loading.value = false;
            }
        };

        // Load available students for binome formation
        const loadAvailableStudents = async () => {
            loading.value = true;
            try {
                const response = await ApiService.get(
                    "/etudiant/binome/available-students"
                );
                availableStudents.value = response.availableStudents || [];
                hasSentRequest.value = response.hasSentRequest || false;
                targetStudentId.value = response.targetStudentId;
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors du chargement des étudiants disponibles"
                );
            } finally {
                loading.value = false;
            }
        };

        // Accept a binome request
        const acceptRequest = async (requestId) => {
            processingRequestId.value = requestId;
            processingAction.value = "accept";
            try {
                const response = await ApiService.post(
                    `/etudiant/binome/accept/${requestId}`
                );

                toast.add({
                    severity: "success",
                    summary: "Demande acceptée",
                    detail: "Vous avez accepté la demande de binôme",
                    life: 3000,
                });

                // Binome is now formed, redirect to sujet selection
                setTimeout(() => {
                    router.push("/etudiant/sujet");
                }, 1500);
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors de l'acceptation de la demande"
                );
            } finally {
                processingRequestId.value = null;
                processingAction.value = null;
            }
        };

        // Reject a binome request
        const rejectRequest = async (requestId) => {
            processingRequestId.value = requestId;
            processingAction.value = "reject";
            try {
                await ApiService.post(`/etudiant/binome/reject/${requestId}`);

                // Remove from local list
                pendingRequests.value = pendingRequests.value.filter(
                    (request) => request.id !== requestId
                );

                toast.add({
                    severity: "info",
                    summary: "Demande refusée",
                    detail: "Vous avez refusé la demande de binôme",
                    life: 3000,
                });

                // If no more pending requests, show choose binome view
                if (pendingRequests.value.length === 0) {
                    hasPendingRequests.value = false;
                    showChooseBinome.value = true;
                    await loadAvailableStudents();
                }
            } catch (error) {
                handleError(error, "Erreur lors du refus de la demande");
            } finally {
                processingRequestId.value = null;
                processingAction.value = null;
            }
        };

        // Send a binome request to another student
        const sendRequest = async (studentId) => {
            processingStudentId.value = studentId;
            try {
                const response = await ApiService.post(
                    "/etudiant/binome/request",
                    studentId
                );

                if (response.success) {
                    toast.add({
                        severity: "success",
                        summary: "Demande envoyée",
                        detail:
                            response.message ||
                            "Votre demande de binôme a été envoyée",
                        life: 3000,
                    });

                    // If auto-accepted (there was a mutual request)
                    if (response.binome) {
                        toast.add({
                            severity: "info",
                            summary: "Binôme formé",
                            detail: "Une demande mutuelle existait. Votre binôme a été formé automatiquement",
                            life: 4000,
                        });

                        // Redirect to sujet selection
                        setTimeout(() => {
                            router.push("/etudiant/sujet");
                        }, 2000);
                    } else {
                        // Just update sent request status
                        hasSentRequest.value = true;
                    }
                }
            } catch (error) {
                handleError(error, "Erreur lors de l'envoi de la demande");
            } finally {
                processingStudentId.value = null;
            }
        };

        // Cancel a sent binome request
        const cancelRequest = async () => {
            processingCancel.value = true;
            try {
                await ApiService.post("/etudiant/binome/cancel");

                toast.add({
                    severity: "info",
                    summary: "Demande annulée",
                    detail: "Vous avez annulé votre demande de binôme",
                    life: 3000,
                });

                // Reset sent request status
                hasSentRequest.value = false;
                targetStudentId.value = null;

                // Reload available students
                await loadAvailableStudents();
            } catch (error) {
                handleError(error, "Erreur lors de l'annulation de la demande");
            } finally {
                processingCancel.value = false;
            }
        };

        // Continue alone (create single-student binome)
        const continueAlone = async () => {
            processingAlone.value = true;
            try {
                const response = await ApiService.post(
                    "/etudiant/binome/continue-alone"
                );

                toast.add({
                    severity: "success",
                    summary: "Binôme créé",
                    detail: "Vous avez choisi de continuer seul. Votre binôme a été créé",
                    life: 3000,
                });

                // Redirect to sujet selection
                setTimeout(() => {
                    router.push("/etudiant/sujet");
                }, 1500);
            } catch (error) {
                handleError(
                    error,
                    "Erreur lors de la création du binôme individuel"
                );
            } finally {
                processingAlone.value = false;
            }
        };

        // Go to choose binome view (from pending requests)
        const goToChooseBinome = () => {
            hasPendingRequests.value = false;
            showChooseBinome.value = true;
            loadAvailableStudents();
        };

        // Handle search from header
        const handleHeaderSearch = (query) => {
            searchQuery.value = query;
        };

        // Error handling
        const handleError = (error, defaultMessage) => {
            console.error(defaultMessage, error);
            let message = defaultMessage;

            if (error.message) {
                message = error.message;
            } else if (
                error.response &&
                error.response.data &&
                error.response.data.message
            ) {
                message = error.response.data.message;
            }

            toast.add({
                severity: "error",
                summary: "Erreur",
                detail: message,
                life: 5000,
            });
        };

        return {
            // State
            loading,
            hasPendingRequests,
            pendingRequests,
            showChooseBinome,
            availableStudents,
            hasSentRequest,
            searchQuery,
            processingRequestId,
            processingStudentId,
            processingAction,
            processingAlone,
            processingCancel,
            isProcessingAnyRequest,
            isProcessingAnyStudent,
            filteredStudents,

            // Methods
            acceptRequest,
            rejectRequest,
            sendRequest,
            cancelRequest,
            continueAlone,
            goToChooseBinome,
            handleHeaderSearch,
        };
    },
};
</script>

<style scoped>
.etudiant-view {
    margin: 0 auto;
}

.view-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
</style>