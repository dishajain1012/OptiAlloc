function LandingPage() {
  return (
    <main>

      {/* HERO SECTION */}
      <section className="hero">

        <div className="hero-content">
            <div className="hero-tag">
                INTELLIGENT RESOURCE SCHEDULING
            </div>
          <h1>
            Smarter Resource Allocation.
            <br />
            <span>Zero Scheduling Conflicts.</span>
            </h1>

          <p>
            OptiAlloc intelligently allocates limited resources
            based on availability, capacity, priority and constraints.
          </p>

          <div className="hero-buttons">

            <a href="/login" className="primary-btn">
              Get Started
            </a>

            <a href="#how-it-works" className="secondary-btn">
              Learn More
            </a>

          </div>

        </div>

      </section>


      {/* FEATURES */}
      <section id="features" className="features-section">

        <h2>Why OptiAlloc?</h2>

        <p className="section-subtitle">
          Designed to solve real-world resource allocation problems.
        </p>

        <div className="feature-grid">

          <div className="feature-card">
            <h3>Smart Allocation</h3>
            <p>
              Automatically selects the most suitable resource
              based on capacity, availability and requirements.
            </p>
          </div>

          <div className="feature-card">
            <h3>Conflict Detection</h3>
            <p>
              Detects overlapping bookings and prevents
              double allocation of resources.
            </p>
          </div>

          <div className="feature-card">
            <h3>Priority Based</h3>
            <p>
              Higher-priority requests are considered first
              during scheduling.
            </p>
          </div>

          <div className="feature-card">
            <h3>Real-Time Scheduling</h3>
            <p>
              Recalculates resource allocation when availability
              or requests change.
            </p>
          </div>

        </div>

      </section>


      {/* HOW IT WORKS */}
      <section id="how-it-works" className="process-section">

        <h2>How It Works</h2>

        <div className="process-grid">

          <div className="process-card">
            <span>01</span>
            <h3>Submit Request</h3>
            <p>
              A user specifies the required resource,
              capacity and time slot.
            </p>
          </div>

          <div className="process-card">
            <span>02</span>
            <h3>Validate Constraints</h3>
            <p>
              OptiAlloc checks availability, capacity,
              equipment and scheduling conflicts.
            </p>
          </div>

          <div className="process-card">
            <span>03</span>
            <h3>Optimize</h3>
            <p>
              The scheduling engine ranks suitable resources
              and selects the best available option.
            </p>
          </div>

          <div className="process-card">
            <span>04</span>
            <h3>Allocate</h3>
            <p>
              The selected resource is booked and the request
              status is updated.
            </p>
          </div>

        </div>

      </section>


      {/* ABOUT */}
      <section id="about" className="about-section">

        <h2>About OptiAlloc</h2>

        <p>
          OptiAlloc is a constraint-based resource allocation
          and scheduling platform designed to efficiently manage
          competing requests for limited resources.
        </p>

      </section>


      {/* CTA */}
      <section className="cta-section">

        <h2>Ready to optimize your resources?</h2>

        <a href="/login" className="primary-btn">
          Get Started
        </a>

      </section>

    </main>
  );
}

export default LandingPage;