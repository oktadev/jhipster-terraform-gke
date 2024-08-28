terraform {
  required_version = ">=1.8"

  required_providers {

    auth0 = {
      source  = "auth0/auth0"
      version = "~> 0.49.0"
    }
  }
}
