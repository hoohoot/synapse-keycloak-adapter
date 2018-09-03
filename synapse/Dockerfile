FROM matrixdotorg/synapse:v0.33.3

ENV REST_AUTH_PROVIDER_VERSION 0.1.1

RUN wget -q --show-progress "https://github.com/kamax-io/matrix-synapse-rest-auth/archive/v$REST_AUTH_PROVIDER_VERSION.tar.gz" \
	&& tar xf "riot-v$RIOT_VERSION.tar.gz" \
	&& rm "riot-v$RIOT_VERSION.tar.gz"

COPY rest_auth_provider.py /usr/local/lib/python2.7/site-packages/

VOLUME ["/data"]
EXPOSE 8008/tcp 8448/tcp
ENTRYPOINT ["/start.py"]
