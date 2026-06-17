<template>
  <div class="landing-container">
    <section class="hero">
      <div class="hero-content">
        <h1>
          Organiza el cuidado de quienes
          <span class="highlight">más importan</span>
        </h1>
        <p>
          La comunidad en Caracas que conecta familias con cuidadores
          verificados para niños, adultos mayores y mascotas.
        </p>
        <div class="cta-group">
          <RouterLink to="/registro" class="btn-primary"
            >Registrarme ahora</RouterLink
          >
          <RouterLink to="/login" class="btn-secondary"
            >Iniciar sesión</RouterLink
          >
        </div>
      </div>
      <div class="hero-image">
        <div class="image-blob">
          <img
            src="https://images.pexels.com/photos/3768131/pexels-photo-3768131.jpeg?auto=compress&cs=tinysrgb&w=800"
            alt="Cuidado en el hogar"
          />
        </div>
      </div>
    </section>

    <section class="services">
      <h2>Nuestros Servicios en Caracas</h2>
      <div class="services-grid">
        <div class="service-card">
          <span class="icon">👶</span>
          <h3>Niños</h3>
          <p>Apoyo escolar y cuidado recreativo para los más pequeños.</p>
        </div>
        <div class="service-card">
          <span class="icon">👴</span>
          <h3>Adultos Mayores</h3>
          <p>Compañía y asistencia en actividades diarias con dignidad.</p>
        </div>
        <div class="service-card">
          <span class="icon">🐾</span>
          <h3>Mascotas</h3>
          <p>Paseos y cuidado temporal para tus compañeros fieles.</p>
        </div>
      </div>
    </section>

    <section class="stats">
      <div class="stat-item">
        <span class="number">500+</span>
        <span class="label">Cuidadores</span>
      </div>
      <div class="stat-item">
        <span class="number">1000+</span>
        <span class="label">Familias</span>
      </div>
      <div class="stat-item">
        <span class="number">15 min</span>
        <span class="label">Tiempo de respuesta</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.landing-container {
  display: flex;
  flex-direction: column;
  gap: 80px;
  padding-bottom: 60px;
}

/* HERO */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  padding: 40px 0;
}

.hero-content {
  flex: 1;
}

.hero-content h1 {
  font-size: 3.5rem;
  font-family: "Nunito", sans-serif;
  color: var(--color-heading);
  line-height: 1.1;
  margin-bottom: 24px;
}

.highlight {
  color: var(--color-primary);
}

.hero-content p {
  font-size: 1.25rem;
  color: var(--color-text);
  margin-bottom: 32px;
  max-width: 500px;
}

.cta-group {
  display: flex;
  gap: 16px;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  padding: 14px 28px;
  border-radius: 50px;
  font-weight: 700;
  text-decoration: none;
  transition: transform 0.2s ease;
}

.btn-secondary {
  border: 2px solid var(--color-primary);
  color: var(--color-primary);
  padding: 14px 28px;
  border-radius: 50px;
  font-weight: 700;
  text-decoration: none;
}

.btn-primary:hover {
  transform: translateY(-2px);
  background-color: var(--color-primary-hover);
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.image-blob {
  width: 450px;
  height: 450px;
  background-color: var(--color-primary);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  overflow: hidden;
  box-shadow: 20px 20px 60px rgba(0, 0, 0, 0.1);
}

.image-blob img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* SERVICES */
.services h2 {
  text-align: center;
  font-family: "Nunito", sans-serif;
  font-size: 2.5rem;
  margin-bottom: 40px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.service-card {
  background-color: var(--color-background-soft);
  padding: 40px;
  border-radius: 24px;
  text-align: center;
  border: 1px solid var(--color-border);
}

.service-card .icon {
  font-size: 3rem;
  margin-bottom: 20px;
  display: block;
}

.service-card h3 {
  color: var(--color-heading);
  margin-bottom: 12px;
}

/* STATS */
.stats {
  display: flex;
  justify-content: space-around;
  background-color: #f3f0df; /* Crema del manual */
  padding: 60px;
  border-radius: 32px;
  color: #1f2937;
}

.stat-item {
  text-align: center;
}

.stat-item .number {
  display: block;
  font-size: 3rem;
  font-weight: 800;
  color: var(--color-primary);
}

.stat-item .label {
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

@media (max-width: 968px) {
  .hero {
    flex-direction: column;
    text-align: center;
  }
  .hero-content p {
    margin: 0 auto 32px;
  }
  .cta-group {
    justify-content: center;
  }
  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>
