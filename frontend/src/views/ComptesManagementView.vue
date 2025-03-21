<template>
    <div class="comptes-management">
      <Toast />
      <ConfirmDialog />
      
      <div class="header-section">
        <h1 class="page-title">
          Gestion des Utilisateurs
          <div class="filter-controls">
            <Dropdown 
              v-model="selectedRole" 
              :options="roleOptions" 
              optionLabel="name" 
              optionValue="value"
              placeholder="Sélectionner un rôle" 
              class="role-dropdown" 
            />
            
            <Dropdown 
              v-if="selectedRole === 'ETUDIANT'"
              v-model="selectedClass" 
              :options="classes" 
              optionLabel="nom" 
              optionValue="id"
              placeholder="Sélectionner une filière" 
              class="class-dropdown" 
            />
          </div>
        </h1>
      </div>
      
      <div class="action-bar">
        <span class="p-input-icon-left search-container">
          <i class="pi pi-search" />
          <InputText 
            v-model="searchQuery" 
            placeholder="Rechercher un utilisateur..." 
            class="search-input"
          />
        </span>
        <Button 
          label="Ajouter un utilisateur" 
          icon="pi pi-plus" 
          class="p-button-primary add-btn"
          @click="openAddModal" 
        />
      </div>
  
      <!-- Data Table with PrimeVue -->
      <Card class="table-card">
        <template #content>
          <DataTable 
            :value="filteredComptes" 
            :loading="loading" 
            responsiveLayout="scroll"
            stripedRows 
            :paginator="filteredComptes.length > 10" 
            :rows="10" 
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
            :rowsPerPageOptions="[10, 20, 50]"
            currentPageReportTemplate="Montrant {first} à {last} sur {totalRecords} utilisateurs"
            emptyMessage="Aucun utilisateur trouvé"
          >
            <Column field="nom" header="Nom" sortable style="min-width: 8rem"></Column>
            <Column field="prenom" header="Prénom" sortable style="min-width: 8rem"></Column>
            <Column field="email" header="Email" sortable style="min-width: 14rem"></Column>
            <Column header="Filière" style="min-width: 10rem">
              <template #body="slotProps">
                {{ getUserFiliere(slotProps.data) }}
              </template>
            </Column>
            <Column header="Actions" :exportable="false" style="min-width: 8rem">
              <template #body="slotProps">
                <div class="action-buttons">
                  <Button 
                    icon="pi pi-eye" 
                    class="p-button-rounded p-button-outlined p-button-secondary mr-2" 
                    @click="openViewModal(slotProps.data)" 
                    tooltip="Voir"
                    tooltipOptions="top"
                  />
                  <Button 
                    icon="pi pi-pencil" 
                    class="p-button-rounded p-button-outlined p-button-info mr-2" 
                    @click="openEditModal(slotProps.data)" 
                    tooltip="Modifier"
                    tooltipOptions="top"
                  />
                  <Button 
                    icon="pi pi-trash" 
                    class="p-button-rounded p-button-outlined p-button-danger" 
                    @click="confirmDelete(slotProps.data)" 
                    tooltip="Supprimer"
                    tooltipOptions="top"
                  />
                </div>
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
  
      <!-- Add User Dialog -->
      <Dialog 
        v-model:visible="showAddModal" 
        header="Ajouter un Nouvel Utilisateur" 
        :modal="true" 
        class="user-dialog"
        :style="{ width: '600px' }"
        :closable="false"
      >
        <div class="field-container">
          <div class="p-field">
            <label for="role">Rôle</label>
            <Dropdown 
              id="role" 
              v-model="newCompte.role" 
              :options="roleOptions" 
              optionLabel="name" 
              optionValue="value"
              placeholder="Sélectionner un rôle" 
              class="w-full"
              required
              @change="handleRoleChange"
            />
          </div>
          
          <div class="p-field">
            <label for="nom">Nom</label>
            <InputText 
              id="nom" 
              v-model="newCompte.nom" 
              class="w-full" 
              placeholder="Nom"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="prenom">Prénom</label>
            <InputText 
              id="prenom" 
              v-model="newCompte.prenom" 
              class="w-full" 
              placeholder="Prénom"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="email">Email</label>
            <InputText 
              id="email" 
              v-model="newCompte.email" 
              class="w-full" 
              placeholder="Email"
              type="email"
              required
            />
          </div>
          
          <div v-if="newCompte.role && newCompte.role !== 'ETUDIANT'" class="p-field">
            <label for="cni">CNI</label>
            <InputText 
              id="cni" 
              v-model="newCompte.cni" 
              class="w-full" 
              placeholder="CNI"
              required
            />
          </div>
          
          <div v-if="newCompte.role === 'ETUDIANT'" class="p-field">
            <label for="cne">CNE</label>
            <InputText 
              id="cne" 
              v-model="newCompte.cne" 
              class="w-full" 
              placeholder="CNE"
              required
            />
          </div>
          
          <div v-if="newCompte.role === 'ETUDIANT'" class="p-field">
            <label for="filiere">Filière</label>
            <Dropdown 
              id="filiere" 
              v-model="newCompte.filiere" 
              :options="classes" 
              optionLabel="nom" 
              optionValue="id"
              placeholder="Sélectionner une filière" 
              class="w-full"
              required
            />
          </div>
          
          <div v-if="newCompte.role === 'ETUDIANT'" class="p-field">
            <label for="dateNaissance">Date de Naissance</label>
            <Calendar 
              id="dateNaissance" 
              v-model="newCompte.dateNaissance" 
              dateFormat="dd/mm/yy"
              class="w-full" 
              placeholder="Date de naissance"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="motDePasse">Mot de Passe</label>
            <Password 
              id="motDePasse" 
              v-model="newCompte.motDePasse" 
              class="w-full" 
              placeholder="Mot de passe"
              :feedback="false"
              required
              toggleMask
            />
          </div>
        </div>
        
        <template #footer>
          <Button 
            label="Annuler" 
            icon="pi pi-times" 
            class="p-button-text" 
            @click="closeModals" 
          />
          <Button 
            label="Ajouter" 
            icon="pi pi-check" 
            class="p-button-primary" 
            @click="addCompte" 
            :loading="submitting"
          />
        </template>
      </Dialog>
      
      <!-- Edit User Dialog -->
      <Dialog 
        v-model:visible="showEditModal" 
        header="Modifier l'Utilisateur" 
        :modal="true" 
        class="user-dialog"
        :style="{ width: '600px' }"
        :closable="false"
      >
        <div v-if="editingCompte" class="field-container">
          <div class="p-field">
            <label for="edit-nom">Nom</label>
            <InputText 
              id="edit-nom" 
              v-model="editingCompte.nom" 
              class="w-full" 
              placeholder="Nom"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="edit-prenom">Prénom</label>
            <InputText 
              id="edit-prenom" 
              v-model="editingCompte.prenom" 
              class="w-full" 
              placeholder="Prénom"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="edit-email">Email</label>
            <InputText 
              id="edit-email" 
              v-model="editingCompte.email" 
              class="w-full" 
              placeholder="Email"
              type="email"
              required
            />
          </div>
          
          <div v-if="editingCompte.role && editingCompte.role !== 'ETUDIANT'" class="p-field">
            <label for="edit-cni">CNI</label>
            <InputText 
              id="edit-cni" 
              v-model="editingCompte.cni" 
              class="w-full" 
              placeholder="CNI"
              required
            />
          </div>
          
          <div v-if="editingCompte.role === 'ETUDIANT'" class="p-field">
            <label for="edit-cne">CNE</label>
            <InputText 
              id="edit-cne" 
              v-model="editingCompte.cne" 
              class="w-full" 
              placeholder="CNE"
              required
            />
          </div>
          
          <div v-if="editingCompte.role === 'ETUDIANT'" class="p-field">
            <label for="edit-filiere">Filière</label>
            <Dropdown 
              id="edit-filiere" 
              v-model="editingCompte.filiere" 
              :options="classes" 
              optionLabel="nom" 
              optionValue="id"
              placeholder="Sélectionner une filière" 
              class="w-full"
              required
            />
          </div>
          
          <div v-if="editingCompte.role === 'ETUDIANT'" class="p-field">
            <label for="edit-dateNaissance">Date de Naissance</label>
            <Calendar 
              id="edit-dateNaissance" 
              v-model="editingCompte.dateNaissance" 
              dateFormat="dd/mm/yy"
              class="w-full" 
              placeholder="Date de naissance"
              required
            />
          </div>
          
          <div class="p-field">
            <label for="edit-motDePasse">Mot de Passe (laisser vide pour ne pas changer)</label>
            <Password 
              id="edit-motDePasse" 
              v-model="editingCompte.motDePasse" 
              class="w-full" 
              placeholder="Nouveau mot de passe"
              :feedback="false"
              toggleMask
            />
          </div>
        </div>
        
        <template #footer>
          <Button 
            label="Annuler" 
            icon="pi pi-times" 
            class="p-button-text" 
            @click="closeModals" 
          />
          <Button 
            label="Sauvegarder" 
            icon="pi pi-check" 
            class="p-button-primary" 
            @click="saveEditedCompte" 
            :loading="submitting"
          />
        </template>
      </Dialog>
      
      <!-- View User Dialog -->
      <Dialog 
        v-model:visible="showViewModal" 
        :header="`Détails de l'utilisateur: ${viewingCompte?.prenom} ${viewingCompte?.nom}`" 
        :modal="true" 
        class="user-dialog"
        :style="{ width: '500px' }"
      >
        <div v-if="viewingCompte" class="user-details">
          <div class="detail-item">
            <h3>Informations générales</h3>
            <p><strong>Nom:</strong> {{ viewingCompte.nom }}</p>
            <p><strong>Prénom:</strong> {{ viewingCompte.prenom }}</p>
            <p><strong>Email:</strong> {{ viewingCompte.email }}</p>
            <p><strong>Rôle:</strong> {{ getRoleLabel(viewingCompte.role) }}</p>
          </div>
          
          <div v-if="viewingCompte.role !== 'ETUDIANT'" class="detail-item">
            <p><strong>CNI:</strong> {{ viewingCompte.cni }}</p>
          </div>
          
          <div v-if="viewingCompte.role === 'ETUDIANT'" class="detail-item">
            <h3>Informations étudiant</h3>
            <p><strong>CNE:</strong> {{ viewingCompte.cne }}</p>
            <p><strong>Date de naissance:</strong> {{ formatDate(viewingCompte.dateNaissance) }}</p>
            <p><strong>Filière:</strong> {{ getUserFiliere(viewingCompte) }}</p>
          </div>
        </div>
        
        <template #footer>
          <Button 
            label="Fermer" 
            icon="pi pi-times" 
            class="p-button-text" 
            @click="showViewModal = false" 
          />
        </template>
      </Dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useToast } from 'primevue/usetoast';
  import { useConfirm } from 'primevue/useconfirm';
  import ApiService from '@/services/ApiService';
  
  // Component state
  const comptes = ref([]);
  const etudiants = ref([]);
  const classes = ref([]);
  const selectedRole = ref('ETUDIANT');
  const selectedClass = ref(null);
  const newCompte = ref({
    nom: '',
    prenom: '',
    email: '',
    cni: '',
    cne: '',
    dateNaissance: null,
    motDePasse: '',
    role: 'ETUDIANT',
    filiere: null
  });
  const editingCompte = ref(null);
  const viewingCompte = ref(null);
  const showEditModal = ref(false);
  const showAddModal = ref(false);
  const showViewModal = ref(false);
  const loading = ref(false);
  const submitting = ref(false);
  const searchQuery = ref('');
  
  // Role options for dropdown
  const roleOptions = [
    { name: 'Étudiants', value: 'ETUDIANT' },
    { name: 'Encadrants', value: 'ENCADRANT' },
    { name: 'Jurys', value: 'JURY' },
    { name: 'Chefs de département', value: 'CHEF_DE_DEPARTEMENT' },
    { name: 'Administrateurs', value: 'ADMIN' }
  ];
  
  // Services
  const toast = useToast();
  const confirm = useConfirm();
  
  // Computed filtered comptes
  const filteredComptes = computed(() => {
    const query = searchQuery.value.toLowerCase();
    
    // Filter by role
    let filtered = comptes.value.filter(compte => compte.role === selectedRole.value);
    
    // Filter students by class if a class is selected
    if (selectedRole.value === 'ETUDIANT' && selectedClass.value) {
      filtered = filtered.filter(compte => {
        const etudiant = etudiants.value.find(e => e.id === compte.id);
        return etudiant && etudiant.filiere && etudiant.filiere.id === selectedClass.value;
      });
    }
    
    // Filter by search query
    if (query) {
      filtered = filtered.filter(compte => 
        compte.nom.toLowerCase().includes(query) || 
        compte.prenom.toLowerCase().includes(query) ||
        compte.email.toLowerCase().includes(query)
      );
    }
    
    return filtered;
  });
  
  // Fetch data on component mount
  onMounted(async () => {
    await Promise.all([
      fetchComptes(),
      fetchEtudiants(),
      fetchClasses()
    ]);
    
    // Set default selected class
    if (classes.value.length > 0) {
      selectedClass.value = classes.value[0].id;
    }
  });
  
  // Methods for fetching data
  async function fetchComptes() {
    loading.value = true;
    try {
      const response = await ApiService.get('/api/utilisateurs');
      comptes.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des utilisateurs');
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchEtudiants() {
    try {
      const response = await ApiService.get('/api/etudiant');
      etudiants.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des étudiants');
    }
  }
  
  async function fetchClasses() {
    try {
      const response = await ApiService.get('/api/filiere');
      classes.value = response;
    } catch (error) {
      handleApiError(error, 'Erreur lors du chargement des filières');
    }
  }
  
  // Helper methods for data display
  function getUserFiliere(user) {
    if (user.role !== 'ETUDIANT') return 'N/A';
    
    const etudiant = etudiants.value.find(e => e.id === user.id);
    if (!etudiant || !etudiant.filiere) return 'N/A';
    
    const filiere = classes.value.find(c => c.id === etudiant.filiere.id);
    return filiere ? filiere.nom : 'N/A';
  }
  
  function getRoleLabel(role) {
    const option = roleOptions.find(o => o.value === role);
    return option ? option.name : role;
  }
  
  function formatDate(date) {
    if (!date) return 'N/A';
    
    // If it's already a string in a date format, return it
    if (typeof date === 'string' && date.includes('-')) {
      return new Date(date).toLocaleDateString('fr-FR');
    }
    
    // If it's a Date object
    if (date instanceof Date) {
      return date.toLocaleDateString('fr-FR');
    }
    
    return 'N/A';
  }
  
  // CRUD operations
  async function addCompte() {
    if (!validateCompteData(newCompte.value, true)) return;
    
    submitting.value = true;
    try {
      // Prepare request based on role
      const accountData = {
        nom: newCompte.value.nom,
        prenom: newCompte.value.prenom,
        email: newCompte.value.email,
        motDePasse: newCompte.value.motDePasse,
        role: newCompte.value.role
      };
      
      // Add role-specific fields
      if (newCompte.value.role === 'ETUDIANT') {
        accountData.cne = newCompte.value.cne;
        accountData.dateNaissance = newCompte.value.dateNaissance;
      } else {
        accountData.cni = newCompte.value.cni;
      }
      
      // Register the user
      const response = await ApiService.post('/api/auth/register', accountData);
      
      // If this is a student, we need to associate with a filière
      if (newCompte.value.role === 'ETUDIANT' && newCompte.value.filiere) {
        // Create etudiant entry
        await ApiService.post('/api/etudiant', {
          id: response.id,
          utilisateur: { id: response.id },
          filiere: { id: newCompte.value.filiere }
        });
      }
      
      // Add to local list
      comptes.value.push(response);
      
      // Reset form and close modal
      resetNewCompte();
      showAddModal.value = false;
      
      // Refresh data
      await Promise.all([fetchComptes(), fetchEtudiants()]);
      
      // Show success message
      toast.add({
        severity: 'success',
        summary: 'Utilisateur ajouté',
        detail: 'L\'utilisateur a été ajouté avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, "Erreur lors de l'ajout de l'utilisateur");
    } finally {
      submitting.value = false;
    }
  }
  
  async function saveEditedCompte() {
    if (!validateCompteData(editingCompte.value, false)) return;
    
    submitting.value = true;
    try {
      // Prepare base user data
      const userData = {
        id: editingCompte.value.id,
        nom: editingCompte.value.nom,
        prenom: editingCompte.value.prenom,
        email: editingCompte.value.email,
        role: editingCompte.value.role
      };
      
      // Add role-specific fields
      if (editingCompte.value.role === 'ETUDIANT') {
        userData.cne = editingCompte.value.cne;
        userData.dateNaissance = editingCompte.value.dateNaissance;
      } else {
        userData.cni = editingCompte.value.cni;
      }
      
      // Add password only if provided
      if (editingCompte.value.motDePasse) {
        userData.motDePasse = editingCompte.value.motDePasse;
      }
      
      // Update user
      const response = await ApiService.put(`/api/utilisateurs/${editingCompte.value.id}`, userData);
      
      // If student and filiere changed, update etudiant entry
      if (editingCompte.value.role === 'ETUDIANT' && editingCompte.value.filiere) {
        const etudiant = etudiants.value.find(e => e.id === editingCompte.value.id);
        
        if (etudiant) {
          // Update existing etudiant
          await ApiService.put(`/api/etudiant/${etudiant.id}`, {
            id: etudiant.id,
            utilisateur: { id: editingCompte.value.id },
            filiere: { id: editingCompte.value.filiere }
          });
        } else {
          // Create new etudiant entry
          await ApiService.post('/api/etudiant', {
            id: editingCompte.value.id,
            utilisateur: { id: editingCompte.value.id },
            filiere: { id: editingCompte.value.filiere }
          });
        }
      }
      
      // Update local data
      const index = comptes.value.findIndex(c => c.id === editingCompte.value.id);
      if (index !== -1) {
        comptes.value[index] = response;
      }
      
      // Close modal
      showEditModal.value = false;
      
      // Refresh data
      await Promise.all([fetchComptes(), fetchEtudiants()]);
      
      // Show success message
      toast.add({
        severity: 'success',
        summary: 'Utilisateur mis à jour',
        detail: 'L\'utilisateur a été modifié avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, 'Erreur lors de la modification de l\'utilisateur');
    } finally {
      submitting.value = false;
    }
  }
  
  function confirmDelete(compte) {
    confirm.require({
      message: `Êtes-vous sûr de vouloir supprimer l'utilisateur "${compte.prenom} ${compte.nom}"?`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptClass: 'p-button-danger',
      accept: () => deleteCompte(compte),
      reject: () => {/* do nothing */}
    });
  }
  
  async function deleteCompte(compte) {
    try {
      await ApiService.delete(`/api/utilisateurs/${compte.id}`);
      
      // Update local data
      comptes.value = comptes.value.filter(c => c.id !== compte.id);
      
      if (compte.role === 'ETUDIANT') {
        etudiants.value = etudiants.value.filter(e => e.id !== compte.id);
      }
      
      toast.add({
        severity: 'success',
        summary: 'Utilisateur supprimé',
        detail: 'L\'utilisateur a été supprimé avec succès',
        life: 3000
      });
    } catch (error) {
      handleApiError(error, 'Erreur lors de la suppression de l\'utilisateur');
    }
  }
  
  // UI control methods
  function openAddModal() {
    resetNewCompte();
    showAddModal.value = true;
  }
  
  function openEditModal(compte) {
    // Create a deep copy to avoid modifying the original object
    editingCompte.value = JSON.parse(JSON.stringify(compte));
    
    // For students, get the filiere ID
    if (compte.role === 'ETUDIANT') {
      const etudiant = etudiants.value.find(e => e.id === compte.id);
      if (etudiant && etudiant.filiere) {
        editingCompte.value.filiere = etudiant.filiere.id;
      }
    }
    
    // Clear password as we don't want to show the current one
    editingCompte.value.motDePasse = '';
    
    showEditModal.value = true;
  }
  
  function openViewModal(compte) {
    // Create a deep copy to avoid modifying the original object
    viewingCompte.value = JSON.parse(JSON.stringify(compte));
    showViewModal.value = true;
  }
  
  function closeModals() {
    showEditModal.value = false;
    showAddModal.value = false;
  }
  
  function handleRoleChange() {
    // Reset role-specific fields when role changes
    if (newCompte.value.role === 'ETUDIANT') {
      newCompte.value.cni = '';
    } else {
      newCompte.value.cne = '';
      newCompte.value.dateNaissance = null;
      newCompte.value.filiere = null;
    }
  }
  
  function resetNewCompte() {
    newCompte.value = {
      nom: '',
      prenom: '',
      email: '',
      cni: '',
      cne: '',
      dateNaissance: null,
      motDePasse: '',
      role: selectedRole.value,
      filiere: selectedClass.value
    };
  }
  
  // Validation
  function validateCompteData(compte, isNew) {
    if (!compte.nom || compte.nom.trim() === '') {
      showValidationError('Le nom est obligatoire');
      return false;
    }
    
    if (!compte.prenom || compte.prenom.trim() === '') {
      showValidationError('Le prénom est obligatoire');
      return false;
    }
    
    if (!compte.email || compte.email.trim() === '') {
      showValidationError('L\'email est obligatoire');
      return false;
    }
    
    if (!validateEmail(compte.email)) {
      showValidationError('Veuillez entrer une adresse email valide');
      return false;
    }
    
    if (isNew && (!compte.motDePasse || compte.motDePasse.trim() === '')) {
      showValidationError('Le mot de passe est obligatoire');
      return false;
    }
    
    if (compte.role === 'ETUDIANT') {
      if (!compte.cne || compte.cne.trim() === '') {
        showValidationError('Le CNE est obligatoire pour les étudiants');
        return false;
      }
      
      if (!compte.dateNaissance) {
        showValidationError('La date de naissance est obligatoire pour les étudiants');
        return false;
      }
      
      if (!compte.filiere) {
        showValidationError('La filière est obligatoire pour les étudiants');
        return false;
      }
    } else {
      if (!compte.cni || compte.cni.trim() === '') {
        showValidationError('Le CNI est obligatoire');
        return false;
      }
    }
    
    return true;
  }
  
  function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
  
  // Error handling
  function handleApiError(error, defaultMessage) {
    console.error(defaultMessage, error);
    
    let errorMessage = defaultMessage;
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message;
    }
    
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: errorMessage,
      life: 5000
    });
  }
  
  function showValidationError(message) {
    toast.add({
      severity: 'warn',
      summary: 'Validation',
      detail: message,
      life: 5000
    });
  }
  </script>
  
  <style scoped>
  .comptes-management {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
  }
  
  .header-section {
    margin-bottom: 2rem;
  }
  
  .page-title {
    color: var(--primary-color);
    font-size: 2rem;
    margin-bottom: 0.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .filter-controls {
    display: flex;
    gap: 1rem;
    margin-left: 1rem;
  }
  
  .role-dropdown, .class-dropdown {
    display: inline-flex;
    min-width: 200px;
  }
  
  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    gap: 1rem;
  }
  
  .search-container {
    flex: 1;
  }
  
  .search-input {
    width: 100%;
  }
  
  .table-card {
    margin-bottom: 2rem;
  }
  
  .action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
  }
  
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
  
  .user-details .detail-item {
    margin-bottom: 1.5rem;
  }
  
  .user-details h3 {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: var(--primary-color);
  }
  
  .user-details p {
    margin: 0;
    line-height: 1.5;
    margin-bottom: 0.5rem;
  }
  
  /* Responsive adjustments */
  @media screen and (max-width: 768px) {
    .comptes-management {
      padding: 1rem;
    }
    
    .page-title {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
    
    .filter-controls {
      flex-direction: column;
      margin-left: 0;
      margin-top: 1rem;
    }
    
    .action-bar {
      flex-direction: column;
    }
    
    .action-buttons {
      flex-direction: column;
    }
  }
</style>