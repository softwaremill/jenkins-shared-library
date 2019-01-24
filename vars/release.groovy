def setProperties(Map params) {
    properties([
        parameters([
            string(defaultValue: '', description: 'Release version', name: 'RELEASE_VERSION', trim: false),
            string(defaultValue: '', description: 'Next release version', name: 'NEXT_VERSION', trim: false)
        ])
    ])
}

def getCache(Map params) {

    stage('Get sbt cache') {
         sh """
             mkdir /sbt-cache/ivy2
             gsutil cp gs://${params.projectId}/${params.appName}/cache.tar.gz .
             tar xzf cache.tar.gz -C /sbt-cache
         """
    }
}

def publishCache(Map params) {

    stage('Publish sbt cache') {
         sh """
             tar czf /sbt-cache/cache.tar.gz -C /sbt-cache coursier ivy2
             gsutil cp /sbt-cache/cache.tar.gz gs://${params.projectId}/${params.appName}/
         """
    }
}

def checkout(Map params){
    checkout([
            $class: 'GitSCM',
            branches: [[name: '*/master']],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[$class: 'LocalBranch', localBranch: 'master']],
            submoduleCfg: [],
            userRemoteConfigs: [[credentialsId: params.credentialsId, url: params.url]]])

}

def build(Map params){

      sh """
          git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
          git config branch.master.remote origin
          git config branch.master.merge refs/heads/master
          git config user.email "ci@softwaremill.pl"
          git config user.name "softwaremill-ci"
          mkdir -p /root/.sbt/gpg
          mkdir -p /root/.ivy2
          cat $FILE > /root/.sbt/gpg/secring.asc
          cat $CREDS > /root/.ivy2/.credentials
          mkdir ~/.ssh
          ssh-keyscan -H -t rsa github.com  > ~/.ssh/known_hosts
      """

      def command = /java -Dsbt.global.base=\/root\/.sbt -Dsbt.boot.directory=\/root\/.sbt -Dsbt.ivy.home=\/root\/cache\/ivy2 -Dsbt.log.noformat=true -jar $sbtHome\/bin\/sbt-launch.jar  ";set pgpPassphrase := Some(Array($PASS)); release with-defaults release-version ${params.RELEASE_VERSION} next-version ${params.NEXT_VERSION}-SNAPSHOT"/
      sh command

}
