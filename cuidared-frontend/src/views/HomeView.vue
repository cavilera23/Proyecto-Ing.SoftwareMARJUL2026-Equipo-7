<template>
  <div class="landing-container">
    <section class="hero">
      <div class="hero-content">
        <span class="eyebrow">🏡 Red de cuidado P2P · Caracas</span>
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
  gap: 90px;
  padding-bottom: 60px;
}

/* HERO */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 50px;
  padding: 60px 50px;
  background: var(--grad-hero);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
  animation: fadeUp 0.5s ease both;
}

.hero-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.eyebrow {
  display: inline-block;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--color-border);
  color: var(--color-primary-hover);
  font-weight: 600;
  font-size: 13px;
  padding: 7px 14px;
  border-radius: var(--radius-full);
  margin-bottom: 20px;
  box-shadow: var(--shadow-xs);
}

.hero-content h1 {
  font-size: 3.4rem;
  font-family: var(--font-display);
  font-weight: 800;
  color: var(--color-heading);
  line-height: 1.08;
  margin-bottom: 22px;
}

.highlight {
  background: var(--grad-heading);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-content p {
  font-size: 1.2rem;
  color: var(--color-text);
  margin-bottom: 34px;
  max-width: 500px;
}

.cta-group {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.btn-primary {
  background: var(--grad-primary);
  color: white;
  padding: 15px 30px;
  border-radius: var(--radius-full);
  font-weight: 700;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t), box-shadow var(--t);
}

.btn-secondary {
  background: #fff;
  border: 1.5px solid var(--color-border);
  color: var(--color-heading);
  padding: 15px 30px;
  border-radius: var(--radius-full);
  font-weight: 700;
  transition: border-color var(--t), transform var(--t);
}

.btn-primary:hover {
  color: #fff;
  transform: translateY(-3px);
  box-shadow: 0 16px 34px rgba(16, 185, 129, 0.36);
}

.btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary-hover);
  transform: translateY(-3px);
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
}

.image-blob {
  width: 420px;
  height: 420px;
  background: var(--grad-primary);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  animation: blobFloat 8s ease-in-out infinite;
}

@keyframes blobFloat {
  0%, 100% { border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%; }
  50% { border-radius: 60% 40% 40% 60% / 60% 50% 50% 40%; }
}

.image-blob img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* SERVICES */
.services h2 {
  text-align: center;
  font-family: var(--font-display);
  font-size: 2.4rem;
  font-weight: 800;
  margin-bottom: 44px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 26px;
}

.service-card {
  background-color: var(--color-background-soft);
  padding: 40px 32px;
  border-radius: var(--radius-lg);
  text-align: center;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: transform var(--t), box-shadow var(--t), border-color var(--t);
}

.service-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-soft);
}

.service-card .icon {
  display: inline-grid;
  place-items: center;
  width: 72px;
  height: 72px;
  font-size: 2.2rem;
  margin-bottom: 22px;
  background: var(--color-primary-tint);
  border-radius: 20px;
}

.service-card h3 {
  color: var(--color-heading);
  margin-bottom: 12px;
  font-size: 1.3rem;
}

.service-card p {
  color: var(--color-text);
}

/* STATS */
.stats {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 30px;
  background: var(--grad-primary);
  padding: 56px;
  border-radius: var(--radius-xl);
  color: #fff;
  box-shadow: var(--shadow-primary);
}

.stat-item {
  text-align: center;
}

.stat-item .number {
  display: block;
  font-family: var(--font-display);
  font-size: 3rem;
  font-weight: 800;
  color: #fff;
}

.stat-item .label {
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.85);
}

@media (max-width: 968px) {
  .hero {
    flex-direction: column;
    text-align: center;
    padding: 44px 26px;
  }
  .hero-content h1 {
    font-size: 2.6rem;
  }
  .hero-content p {
    margin: 0 auto 32px;
  }
  .cta-group {
    justify-content: center;
  }
  .image-blob {
    width: 300px;
    height: 300px;
  }
  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>
