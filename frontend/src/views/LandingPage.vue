<template>
  <div class="landing-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">Gestion des Projets de Fin d'Études</h1>
        <p class="hero-subtitle">
          Plateforme complète pour la gestion, le suivi et l'évaluation 
          des PFE au sein du département de mathématiques/informatique à l'ESTFBS
        </p>
        <div class="hero-buttons">
          <Button label="Se Connecter" icon="pi pi-user" class="p-button-rounded" @click="navigateTo('/login')" />
          <Button label="En Savoir Plus" icon="pi pi-info-circle" class="p-button-rounded p-button-outlined" @click="scrollToFeatures" />
        </div>
      </div>
      <div class="hero-image">
        <img src="@/assets/illustrations/college project-amico (2).svg" alt="Étudiants travaillant sur un projet" class="hero-img" />
      </div>
    </div>

    <!-- Statistics Strip -->
    <div class="stats-strip">
      <div class="stat-item">
        <div class="stat-icon">
          <i class="pi pi-users"></i>
        </div>
        <div class="stat-value">250+</div>
        <div class="stat-label">Étudiants</div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="pi pi-book"></i>
        </div>
        <div class="stat-value">100+</div>
        <div class="stat-label">Projets</div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="pi pi-briefcase"></i>
        </div>
        <div class="stat-value">30+</div>
        <div class="stat-label">Encadrants</div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="pi pi-building"></i>
        </div>
        <div class="stat-value">15+</div>
        <div class="stat-label">Partenaires</div>
      </div>
    </div>

    <!-- Features Section -->
    <div id="features" class="features-section">
      <div class="section-header">
        <h2>Fonctionnalités Principales</h2>
        <p>Une plateforme complète pour gérer l'ensemble du processus des PFE</p>
      </div>

      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-users"></i>
          </div>
          <h3>Formation des Binômes</h3>
          <p>Système de requêtes pour former et valider des binômes, avec options de travail individuel.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-list"></i>
          </div>
          <h3>Gestion des Sujets</h3>
          <p>Catalogue de sujets par filière avec possibilité de proposer de nouveaux sujets.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-chart-line"></i>
          </div>
          <h3>Suivi des Projets</h3>
          <p>Dépôt de documents intermédiaires et tableau de bord pour visualiser l'avancement.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-calendar"></i>
          </div>
          <h3>Planification des Soutenances</h3>
          <p>Organisation des soutenances avec affectation des jurys et salles.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-check-square"></i>
          </div>
          <h3>Système d'Évaluation</h3>
          <p>Notation des rapports et soutenances avec pondération configurable.</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <i class="pi pi-file"></i>
          </div>
          <h3>Gestion des Documents</h3>
          <p>Soumission et consultation des rapports avec archivage par année académique.</p>
        </div>
      </div>
    </div>

    <!-- For Students Section -->
    <div class="students-section">
      <div class="content-half">
        <h2>Pour les Étudiants</h2>
        <p class="section-subtitle">Votre parcours PFE simplifié</p>
        <ul class="benefits-list">
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Formation simplifiée des binômes et choix de sujets</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Suivi transparent de votre progression</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Dépôt sécurisé de vos documents et rapports</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Retours d'évaluation disponibles en temps réel</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Notifications pour les dates importantes</span>
          </li>
        </ul>
        <Button label="Commencer" icon="pi pi-arrow-right" class="p-button-rounded" @click="navigateTo('/login')" />
      </div>
      <div class="image-half">
        <img src="@/assets/illustrations/college students-rafiki.svg" alt="Étudiants collaborant" class="rounded-image" />
      </div>
    </div>

    <!-- For Faculty Section -->
    <div class="faculty-section">
      <div class="image-half">
        <img src="@/assets/illustrations/Professor-pana.svg" alt="Encadrants et professeurs" class="rounded-image" />
      </div>
      <div class="content-half">
        <h2>Pour les Encadrants et Jurys</h2>
        <p class="section-subtitle">Outils d'évaluation efficaces</p>
        <ul class="benefits-list">
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Tableau de bord pour suivre tous vos étudiants</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Interface d'évaluation intuitive</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Planification simplifiée des soutenances</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Accès centralisé aux documents et rapports</span>
          </li>
          <li>
            <i class="pi pi-check-circle"></i>
            <span>Communication facilitée avec les étudiants</span>
          </li>
        </ul>
        <Button label="Découvrir" icon="pi pi-arrow-right" class="p-button-rounded" @click="navigateTo('/login')" />
      </div>
    </div>

    <!-- Call to Action -->
    <div class="cta-section">
      <h2>Prêt à optimiser la gestion de vos PFE ?</h2>
      <p>Rejoignez la plateforme utilisée par le département de mathématiques/informatique à l'ESTFBS</p>
      <Button label="Se Connecter" icon="pi pi-sign-in" class="p-button-rounded p-button-lg" @click="navigateTo('/login')" />
    </div>

    <!-- Footer -->
<footer class="landing-footer">
  <div class="footer-content">
    <div class="footer-logo">
      <img src="@/assets/pfe_man_logo_whole_final.svg" alt="Logo PFE" class="logo-img" />
      <div class="logo-text">
        <h3>PFE Management</h3>
        <p>Système de Gestion des Projets de Fin d'Études</p>
      </div>
    </div>
    
    <div class="footer-logo estfbs-logo">
      <img src="@/assets/LogoESTFBS.png" alt="Logo ESTFBS" class="logo-img" />
      <div class="contact-info">
        <p><i class="pi pi-map-marker"></i> Hay Tighnari, Route nationale N11 de Casablanca commune de Fkih Ben Salah, Boite Postale 336</p>
        <p><i class="pi pi-globe"></i> <a href="https://estfbs.usms.ac.ma" target="_blank">estfbs.usms.ac.ma</a></p>
      </div>
    </div>
    
    <div class="footer-links">
      <h4>Navigation</h4>
      <ul>
        <li><a href="#" @click.prevent="navigateTo('/')">Accueil</a></li>
        <li><a href="#" @click.prevent="scrollToFeatures">Fonctionnalités</a></li>
        <li><a href="#" @click.prevent="navigateTo('/login')">Se connecter</a></li>
        <li><a href="#" @click.prevent="navigateTo('/reset-password')">Mot de passe oublié</a></li>
      </ul>
    </div>
    
    <div class="footer-contact">
      <h4>Contact</h4>
      <p><i class="pi pi-envelope"></i> estfbs@usms.ma</p>
      <p><i class="pi pi-phone"></i> +212 5 23 43 46 66</p>
      <p><i class="pi pi-phone"></i> +212 5 23 43 49 99</p>
      <p><i class="pi pi-envelope"></i> pfe.management@usms.ma</p>
    </div>
  </div>
  <div class="footer-bottom">
    <p>&copy; 2025 EST Fkih Ben Salah. Tous droits réservés.</p>
  </div>
</footer>

  </div>
</template>

<script>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';

export default {
  components: {
    Button
  },
  setup() {
    const router = useRouter();

    // Navigation methods
    const navigateTo = (path) => {
      router.push(path);
    };

    // Scroll to features section
    const scrollToFeatures = () => {
      document.getElementById('features').scrollIntoView({ 
        behavior: 'smooth' 
      });
    };

    onMounted(() => {
      // Any initialization code if needed
    });

    return {
      navigateTo,
      scrollToFeatures
    };
  }
};
</script>

<style scoped>
.landing-container {
  max-width: 100%;
  overflow-x: hidden;
  margin: 0;
  padding: 0;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: var(--text-color);
  background-color: var(--background-color);
}

/* Hero Section */
.hero-section {
  display: flex;
  align-items: center;
  padding: 4rem 2rem;
  padding-top: calc(75px + 2rem); /* Fixed header height */
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-radius: 0 0 2rem 2rem;
  gap: 2rem;
  margin: 0;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-buttons {
  display: flex;
  gap: 1rem;
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hero-img {
  max-width: 100%;
  width: 50%;
  transition: transform 0.3s;
}

.hero-img:hover {
  transform: translateY(-5px);
}

/* Stats Strip */
.stats-strip {
  display: flex;
  justify-content: space-around;
  padding: 2rem;
  margin: 0 auto;
  max-width: 1200px;
  transform: translateY(-50px);
  background-color: var(--background-color);
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
  padding: 1rem;
}

.stat-icon {
  font-size: 2rem;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 1rem;
  color: #666;
}

/* Features Section */
.features-section {
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-header h2 {
  font-size: 2.2rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.section-header p {
  font-size: 1.1rem;
  color: #666;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
}

.feature-card {
  background-color: var(--background-color);
  padding: 2rem;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 2rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

/* Students and Faculty Sections */
.students-section, .faculty-section {
  display: flex;
  align-items: center;
  padding: 5rem 2rem;
  gap: 3rem;
  max-width: 1200px;
  margin: 0 auto;
}

.faculty-section {
  background-color: #f5f5f5;
  border-radius: 2rem;
  margin: 4rem auto;
}

.content-half {
  flex: 1;
}

.content-half h2 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.section-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 2rem;
}

.benefits-list {
  list-style: none;
  padding: 0;
  margin-bottom: 2rem;
}

.benefits-list li {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.benefits-list li i {
  color: var(--primary-color);
  margin-right: 1rem;
  font-size: 1.2rem;
}

.image-half {
  flex: 1;
  display: flex;
  justify-content: center;
}

.rounded-image {
  max-width: 100%;
}

/* Call to Action */
.cta-section {
  text-align: center;
  padding: 5rem 2rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-radius: 2rem 2rem 0 0;
  margin: 0;
}

.cta-section h2 {
  font-size: 2.2rem;
  margin-bottom: 1rem;
}

.cta-section p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

/* Footer */
.landing-footer {
  background-color: #f2f2f2;
  padding: 4rem 2rem 1rem;
  margin: 0;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto 2rem;
}

.footer-logo {
  flex: 1;
  min-width: 200px;
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
}

.logo-img {
  height: 60px;
  margin-bottom: 15px;
  object-fit: contain;
}

.dark-mode .logo-img {
  filter: brightness(0) invert(0.8);
}

.estfbs-logo {
  flex: 1.5;
}

.logo-text h3 {
  font-size: 1.5rem;
  margin: 0 0 0.5rem;
  color: var(--text-color);
}

.logo-text p {
  color: #666;
}

.contact-info p {
  font-size: 0.85rem;
  line-height: 1.4;
  margin-bottom: 0.5rem;
  color: #666;
  display: flex;
  align-items: flex-start;
}

.contact-info p i {
  margin-right: 0.5rem;
  margin-top: 0.25rem;
}

.contact-info a {
  color: var(--primary-color);
  text-decoration: none;
  transition: color 0.2s;
}

.contact-info a:hover {
  text-decoration: underline;
}

.footer-links, .footer-contact {
  flex: 1;
  min-width: 200px;
  margin-bottom: 1.5rem;
}

.footer-links h4, .footer-contact h4 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: var(--text-color);
  position: relative;
  padding-bottom: 0.5rem;
}

.footer-links h4:after, .footer-contact h4:after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  width: 40px;
  height: 2px;
  background-color: var(--primary-color);
}

.footer-links ul {
  list-style: none;
  padding: 0;
}

.footer-links li {
  margin-bottom: 0.5rem;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  transition: color 0.2s, transform 0.2s;
  display: inline-block;
  position: relative;
  padding-left: 0;
}

.footer-links a:hover {
  color: var(--primary-color);
  transform: translateX(5px);
}

.footer-links a:after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background-color: var(--primary-color);
  transition: width 0.3s ease;
}

.footer-links a:hover:after {
  width: 100%;
}

.footer-contact p {
  display: flex;
  align-items: center;
  color: #666;
  margin-bottom: 0.5rem;
}

.footer-contact i {
  margin-right: 0.5rem;
  color: var(--primary-color);
}

.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid #e0e0e0;
  color: #666;
}

/* Dark Mode Adjustments */
.dark-mode .feature-card,
.dark-mode .stats-strip {
  background-color: #252424;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.dark-mode .faculty-section {
  background-color: rgba(255, 255, 255, 0.03);
}

.dark-mode .landing-footer {
  background-color: #222;
}

.dark-mode .footer-bottom {
  border-top-color: #333;
}

.dark-mode .stat-label,
.dark-mode .section-header p,
.dark-mode .feature-card p,
.dark-mode .section-subtitle,
.dark-mode .footer-logo p,
.dark-mode .footer-links a,
.dark-mode .footer-contact p,
.dark-mode .footer-bottom {
  color: #999;
}

/* Responsive Adjustments */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .footer-logo, .footer-links, .footer-contact {
    flex: 1 0 40%;
  }
}

@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 3rem 1rem;
    padding-top: calc(60px + 1rem);
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-buttons {
    justify-content: center;
  }
  
  .stats-strip {
    flex-wrap: wrap;
    transform: translateY(0);
    margin-top: 2rem;
  }
  
  .stat-item {
    flex: 1 0 40%;
  }
  
  .students-section, .faculty-section {
    flex-direction: column;
    padding: 3rem 1rem;
  }
  
  .faculty-section {
    flex-direction: column-reverse;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .footer-logo, .footer-links, .footer-contact {
    flex: 1 0 100%;
    text-align: center;
  }
  
  .footer-links h4:after, .footer-contact h4:after {
    left: 50%;
    transform: translateX(-50%);
  }
  
  .footer-links ul {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .footer-contact p {
    justify-content: center;
  }
  
  .contact-info p {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 1.8rem;
  }
  
  .hero-buttons {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .stat-item {
    flex: 1 0 100%;
  }
  
  .section-header h2, .cta-section h2 {
    font-size: 1.8rem;
  }
}</style>