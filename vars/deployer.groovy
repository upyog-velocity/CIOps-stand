library 'ci-libs'

def call(Map pipelineParams) {
    echo "Environment: ${pipelineParams.environment}"
    echo "POD_LABEL: ${POD_LABEL}"
    
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
                        /opt/egov/egov-deployer deploy --helm-dir `pwd`/${pipelineParams.helmDir} -c=${env.CLUSTER_CONFIGS}  -e ${pipelineParams.environment} "${env.IMAGES}"
                        ls -al /root/.kube
                    """
                }
            }
            echo "Inside the debug node"
        }
    }
}
