# Deploy Secure Spring Boot Microservices on Google Kubernetes Engine Using Terraform and Kubernetes

This is an example application accompanying the blog post [Deploy Secure Spring Boot Microservices on Google GKE Using Terraform and Kubernetes](https://auth0.com/blog/terraform-gke-java-microservices/) on the [Auth0 developer blog](https://auth0.com/blog/developers/).

**Prerequisites**
- [Java OpenJDK 21](https://jdk.java.net/java-se-ri/21)
- [Auth0 account](https://auth0.com/signup)
- [Auth0 CLI 1.4.0](https://github.com/auth0/auth0-cli#installation)
- [Docker 26.1.2](https://docs.docker.com/desktop/)
- [Google Cloud account](https://cloud.google.com/free)
- [gcloud CLI 437.0.1](https://cloud.google.com/sdk/docs/install)
- [JHipster 8.4.0](https://www.jhipster.tech/)
- [kubectl 1.30.1](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Terraform 1.8.3](https://developer.hashicorp.com/terraform/install)
- [jq 1.6](https://jqlang.github.io/jq/download/)


## Create an GKE cluster and Auth0 application using Terraform

To deploy the stack to Google Kubernetes Engine (GKE), we need to create a cluster. So let's begin by creating a cluster using Terraform.

### Create a cluster

Ensure you have configured your [gcloud CLI](https://cloud.google.com/sdk/docs/install). If not, run the following command:

```bash
gcloud init
```

Edit the file `terraform/auth0.tf` and update the auth0 provider with your Auth0 domain URI:

```terraform
# terraform/auth0.tf
provider "auth0" {
  domain        = "https://<your-auth0-domain>"
  debug         = false
}
```

Now before we can run the scripts we need to create a machine to machine application in Auth0 so that Terraform can communicate with the Auth0 management API. This can be done using the Auth0 CLI. Please note that you also need to have [jq](https://jqlang.github.io/jq/) installed to run the below commands. Run the following commands to create an application after logging into the CLI with the `auth0 login` command:

```bash
# Create a machine to machine application on Auth0
export AUTH0_M2M_APP=$(auth0 apps create \
  --name "Auth0 Terraform Provider" \
  --description "Auth0 Terraform Provider M2M" \
  --type m2m \
  --reveal-secrets \
  --json | jq -r '. | {client_id: .client_id, client_secret: .client_secret}')

# Extract the client ID and client secret from the output.
export AUTH0_CLIENT_ID=$(echo $AUTH0_M2M_APP | jq -r '.client_id')
export AUTH0_CLIENT_SECRET=$(echo $AUTH0_M2M_APP | jq -r '.client_secret')
```

This will create the application and set environment variables for client ID and secret. This application needs to be authorized to use the Auth0 management API. This can be done using the below commands.

```bash
# Get the ID and IDENTIFIER fields of the Auth0 Management API
export AUTH0_MANAGEMENT_API_ID=$(auth0 apis list --json | jq -r 'map(select(.name == "Auth0 Management API"))[0].id')
export AUTH0_MANAGEMENT_API_IDENTIFIER=$(auth0 apis list --json | jq -r 'map(select(.name == "Auth0 Management API"))[0].identifier')
# Get the SCOPES to be authorized
export AUTH0_MANAGEMENT_API_SCOPES=$(auth0 apis scopes list $AUTH0_MANAGEMENT_API_ID --json | jq -r '.[].value' | jq -ncR '[inputs]')

# Authorize the Auth0 Terraform Provider application to use the Auth0 Management API
auth0 api post "client-grants" --data='{"client_id": "'$AUTH0_CLIENT_ID'", "audience": "'$AUTH0_MANAGEMENT_API_IDENTIFIER'", "scope":'$AUTH0_MANAGEMENT_API_SCOPES'}'
```

Initialize, plan and apply the following Terraform configuration:

```bash
cd terraform
# download modules and providers. Initialize state.
terraform init
# see a preview of what will be done
terraform plan -out main.tfplan
# apply the changes
terraform apply main.tfplan
```

The complete provisioning of all resources will take while. Once the AKS cluster is ready, you will see the output variables printed on the console. Get the cluter credentials with the following command:

```bash
gcloud container clusters get-credentials <kubernetes-cluster-name> --location us-east1
```

You should see the cluster details if you run `kdash` or `kubectl get nodes` commands.

## Set up OIDC authentication using Auth0

First get the client ID and secret for the Auth0 application created by Terraform.

```bash
# Client ID
terraform output --json | jq -r '.auth0_webapp_client_id.value'
# Client Secret
terraform output --json | jq -r '.auth0_webapp_client_secret.value'
```

Update `kubernetes/registry-k8s/application-configmap.yml` with the OIDC configuration from above.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: application-config
  namespace: jhipster
#common configuration shared between all applications
data:
  application.yml: |-
    configserver:
      ...
    jhipster:
      security:
        ...
        oauth2:
          audience:
            - https://<your-auth0-domain>/api/v2/
    spring:
      security:
        oauth2:
          client:
            provider:
              oidc:
                # make sure to include the trailing slash
                issuer-uri: https://<your-auth0-domain>/
            registration:
              oidc:
                client-id: <client-id>
                client-secret: <client-secret>
  # app specific configuration
```

## Deploy the microservice stack to AKS

You need to build Docker images for each app. This is specific to the JHipster application used in this tutorial. Navigate to each app folder (**store**, **invoice**, **product**) and run the following command:

```bash
./gradlew bootJar -Pprod jib -Djib.to.image=<docker-repo-uri-or-name>/<image-name>
```

Image names would be `store`, `invoice`, and `product`.

Once the images are pushed to the Docker registry, we can deploy the stack using the handy script provided by JHipster. Navigate to the `kubernetes` folder created by JHipster and run the following command.

```bash
cd kubernetes
./kubectl-apply.sh -f
```

Once the deployments are done, we must wait for the pods to be in **RUNNING** status.

As the Ingress controller requires the inbound traffic to be for the host `store.example.com`, you can test the store service by adding an entry in your _hosts_ file that maps to the gateway public IP:

```shell
terraform output spoke_pip
```

Then navigate to `http://store.example.com` and sign in at Atuh0 with the test user/password jhipster@test.com/passpass$12$12.

### Cleanup

Once you are done with the tutorial, you can delete the cluster and all the resources created using Terraform by running the following commands:

```bash
cd terraform
terraform destroy -auto-approve
```

## Links

This example uses the following open source projects:

- [JHipster](https://www.jhipster.tech)
- [React](https://reactjs.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Terraform](https://www.terraform.io/)

## Help

Please post any questions as comments on the [blog post](), or visit our [Auth0 Developer Forums](https://community.auth0.com/).

## License

Apache 2.0, see [LICENSE](LICENSE).