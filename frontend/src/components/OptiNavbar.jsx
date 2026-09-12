import { Link } from "react-router-dom";

function OptiNavbar() {
  return (
    <nav className="navbar">

      <Link to="/" className="logo">
        OptiAlloc
      </Link>

      <div className="nav-links">

        <a href="/#features">
          Features
        </a>

        <a href="/#how-it-works">
          How It Works
        </a>

        <a href="/#about">
          About
        </a>

        <Link to="/login" className="login-btn">
          Sign In
        </Link>

      </div>

    </nav>
  );
}

export default OptiNavbar;