library 'ci-libs'

def call(Map pipelineParams) {
    echo "Environment: ${pipelineParams.environment}"

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
    volumeMounts:
      - name: kube-config
        mountPath: /root/.kube
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
        node('debug-egov-deployer') {  // Changed the node label to match the container name
            stage('Validate Secret Access') {
                container('debug-egov-deployer') {
                    // Check if the directory exists and list the content
                    sh "ls -al /root/.kube || echo 'Secret not found!'"
                }
            }
            echo "Inside the debug node"
        }
    }
}
