package fyi.hellochristine.purpleairtomqtt.mqtt

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient

class MqttModule: AbstractModule() {

    // Sadly need a type literal here cuz we want Guice to explicitly
    // reference Mqtt5RxClient interface versus the concrete implementation
    override fun configure() {
        bind(object : TypeLiteral<Map<String, Mqtt5RxClient>>(){})
            .toProvider(MqttClientProvider::class.java)
    }
}
