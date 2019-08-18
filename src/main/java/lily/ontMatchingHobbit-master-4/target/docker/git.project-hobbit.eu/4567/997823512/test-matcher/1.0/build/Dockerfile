FROM openjdk:8-jre-alpine
ENV OAEI_MAIN=de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherURL
COPY maven /maven/
WORKDIR /maven
CMD java -cp "test-matcher-1.0.jar:lib/*" org.hobbit.core.run.ComponentStarter de.uni_mannheim.informatik.dws.ontmatching.hobbitwrapper.HobbitWrapper
