library 'ci-libs'

def call(Map pipelineParams) {
    echo "Environment: ${pipelineParams.environment}"
    echo "pipelineParams.helmDir: ${pipelineParams.helmDir}"
    echo "env.CLUSTER_CONFIGS: ${env.CLUSTER_CONFIGS}"
    echo "pipelineParams.environment: ${pipelineParams.environment}"
    echo "env.IMAGES: ${env.IMAGES}"
    podTemplate(yaml: """
kind: Pod
metadata:
  name: debug-egov-deployer
spec:
  containers:
  - name: debug-egov-deployer
    image: egovio/egov-deployer:3-master-931c51ff
    command:
    - cat
    tty: true
    resources:
      requests:
        memory: "256Mi"
        cpu: "200m"
      limits:
        memory: "256Mi"
        cpu: "200m"     
  volumes:
  - name: kube-config
    secret:
        secretName: "${pipelineParams.environment}-kube-config"
"""
    ) {
        node(POD_LABEL) {
            stage('Deploy and Validate') {
                container(name: 'debug-egov-deployer', shell: '/bin/sh') {
                    sh """
                        kubectl get pods -n jenkins
                        kubectl get secrets -n jenkins
                        ls -al /root/.kube
                        cat /root/.kube/config
                    """
                }
            }
            echo "Inside the debug node"
        }
    }
}
