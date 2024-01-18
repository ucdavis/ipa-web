mydir=tmp/certs
if [ ! -e "${mydir}" ]
then
mkdir -p "${mydir}"
fi

curl -sS "https://truststore.pki.rds.amazonaws.com/us-west-2/us-west-2-bundle.pem" > ${mydir}/us-west-2-bundle.pem
awk 'split_after == 1 {n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1}{print > "rds-ca-" n ".pem"}' < ${mydir}/us-west-2-bundle.pem

for CERT in rds-ca-*; do
  alias=$(openssl x509 -noout -text -in $CERT | perl -ne 'next unless /Subject:/; s/.*(CN=|CN = )//; print')
  echo "Importing $alias"
  keytool -import -file ${CERT} -alias "${alias}" -storepass changeit -keystore "$JAVA_HOME/jre/lib/security/cacerts" -noprompt
  rm $CERT
done

rm ${mydir}/us-west-2-bundle.pem
