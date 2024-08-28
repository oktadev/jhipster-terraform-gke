resource "google_compute_network" "default" {
  project = var.project_id
  name    = "example-network"

  auto_create_subnetworks  = false
  enable_ula_internal_ipv6 = true
}

resource "google_compute_subnetwork" "default" {
  project = var.project_id
  name    = "example-subnetwork"

  ip_cidr_range = "10.0.0.0/16"
  region        = var.location

  stack_type       = "IPV4_IPV6"
  ipv6_access_type = "EXTERNAL"

  network = google_compute_network.default.id
  secondary_ip_range {
    range_name    = "services-range"
    ip_cidr_range = "10.1.0.0/24"
  }

  secondary_ip_range {
    range_name    = "pod-ranges"
    ip_cidr_range = "10.2.0.0/16"
  }
}

resource "google_container_cluster" "default" {
  project = var.project_id
  name    = "example-cluster"

  location                 = var.location
  enable_l4_ilb_subsetting = true
  initial_node_count       = 2
  datapath_provider        = "ADVANCED_DATAPATH"

  network    = google_compute_network.default.id
  subnetwork = google_compute_subnetwork.default.id

  node_config {
    machine_type = "e2-standard-2"
  }

  ip_allocation_policy {
    stack_type                    = "IPV4_IPV6"
    services_secondary_range_name = google_compute_subnetwork.default.secondary_ip_range[0].range_name
    cluster_secondary_range_name  = google_compute_subnetwork.default.secondary_ip_range[1].range_name
  }

  # Set `deletion_protection` to `true` will ensure that one cannot
  # accidentally delete this instance by use of Terraform.
  deletion_protection = false
}

