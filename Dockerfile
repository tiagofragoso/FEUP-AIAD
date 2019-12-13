FROM circleci/openjdk:8-node
 
USER root
 
WORKDIR /aiad
 
COPY . ./
 
WORKDIR scripts/

RUN echo "Compiling!"
RUN ./compile.sh
RUN echo "Compiled!"

RUN echo "Running"
CMD [ "/bin/bash", "run_data.sh" ]