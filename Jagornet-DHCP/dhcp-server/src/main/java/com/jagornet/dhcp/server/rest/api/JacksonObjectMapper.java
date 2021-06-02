package com.jagornet.dhcp.server.rest.api;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.FiltersType;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinksType;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.config.xml.V4AddressPool;
import com.jagornet.dhcp.server.config.xml.V4AddressPoolsType;
import com.jagornet.dhcp.server.config.xml.V6AddressPool;
import com.jagornet.dhcp.server.config.xml.V6AddressPoolsType;
import com.jagornet.dhcp.server.config.xml.V6PrefixPool;
import com.jagornet.dhcp.server.config.xml.V6PrefixPoolsType;

public class JacksonObjectMapper {

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

	public JacksonObjectMapper() {
		jsonMapper = createJsonObjectMapper();
		yamlMapper = createYamlObjectMapper();
	}
	
	public ObjectMapper getJsonObjectMapper() {
		return jsonMapper;
	}
	
	public ObjectMapper getYamlObjectMapper() {
		return yamlMapper;
	}
	
	private ObjectMapper createJsonObjectMapper() {
		
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(Feature.ALLOW_COMMENTS);
        mapper.enable(Feature.ALLOW_YAML_COMMENTS);
        
		mapper.setSerializationInclusion(Include.NON_NULL);
		AnnotationIntrospector introspector = new 
				JaxbAnnotationIntrospector(mapper.getTypeFactory());   
		mapper.setAnnotationIntrospector(introspector);

		SimpleModule module = new SimpleModule();
		registerJsonDeserializers(module);
		registerJsonSerializers(module);
		
		mapper.registerModule(module);

		return mapper;
    }
	
	private ObjectMapper createYamlObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
		mapper.setSerializationInclusion(Include.NON_NULL);
		AnnotationIntrospector introspector = 
				new JaxbAnnotationIntrospector(mapper.getTypeFactory());   
		mapper.setAnnotationIntrospector(introspector);

		SimpleModule module = new SimpleModule();
		registerJsonDeserializers(module);
		registerJsonSerializers(module);
		
		mapper.registerModule(module);

		return mapper;
    }
    
    public static void registerJsonDeserializers(SimpleModule module) {
		
    	// need this deserializer to handle convert byte arrays to
    	// hex string instead of default base64 encoding
    	module.addDeserializer(byte[].class, new ByteArrayJsonDeserializer());
		
		// need this deserializer to handle hexValue for JSON and YAML, 
    	// it is handled in XML via the hexBinary XML Schema data type
		module.addDeserializer(OpaqueData.class, new OpaqueDataJsonDeserializer());
		
		/*
		 * Not needed after XML schema change to make bindings
		 * more friendly for JSON and YAML configuration files.
		 * 
		module.addDeserializer(PoliciesType.class, 
				new PoliciesTypeJsonDeserializer());
		module.addDeserializer(LinksType.class, 
				new LinksTypeJsonDeserializer());
		module.addDeserializer(FiltersType.class, 
				new FiltersTypeJsonDeserializer());
		module.addDeserializer(V4AddressPoolsType.class,
				new V4AddressPoolsTypeJsonDeserializer());
		module.addDeserializer(V6AddressPoolsType.class,
				new V6AddressPoolsTypeJsonDeserializer());
		module.addDeserializer(V6PrefixPoolsType.class,
				new V6PrefixPoolsTypeJsonDeserializer());
		*/
    }
    
    public static void registerJsonSerializers(SimpleModule module) {

    	// need this serializer to handle convert byte arrays to
    	// hex string instead of default base64 encoding
    	module.addSerializer(byte[].class, new ByteArrayJsonSerializer());

    	// not sure why OpaqueDataSeriaizer is not needed, but
    	// it appears that Jackson can interpret the hexBinary
    	// XML Schema data type when writing, but not reading?
    	
    	/*
		 * Not needed after XML schema change to make bindings
		 * more friendly for JSON and YAML configuration files.
		 * 
		module.addSerializer(PoliciesType.class, 
				new PoliciesTypeJsonSerializer());
		module.addSerializer(LinksType.class, 
				new LinksTypeJsonSerializer());
		module.addSerializer(FiltersType.class, 
				new FiltersTypeJsonSerializer());
		module.addSerializer(V4AddressPoolsType.class,
				new V4AddressPoolsTypeJsonSerializer());
		module.addSerializer(V6AddressPoolsType.class,
				new V6AddressPoolsTypeJsonSerializer());
		module.addSerializer(V6PrefixPoolsType.class,
				new V6PrefixPoolsTypeJsonSerializer());
		*/
    }
    
    private static final class OpaqueDataJsonDeserializer extends JsonDeserializer<OpaqueData> {
		@Override
		public OpaqueData deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();
			JsonNode opaqueNode = oc.readTree(p);

			OpaqueData data = new OpaqueData();
			JsonNode asciiNode = opaqueNode.get("asciiValue");
			if (asciiNode != null) {
				data.setAsciiValue(asciiNode.asText());
			}
			JsonNode hexNode = opaqueNode.get("hexValue");
			if (hexNode != null) {
				data.setHexValue(DatatypeConverter.parseHexBinary(hexNode.asText()));
			}
			return data;
		}
	}
    
    private static final class PoliciesTypeJsonDeserializer extends JsonDeserializer<PoliciesType> {

		@Override
		public PoliciesType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<Policy>> listTypeRef = new TypeReference<List<Policy>>() {};
			List<Policy> policies = oc.readValue(p, listTypeRef);

			PoliciesType policiesType = new PoliciesType();
			policiesType.getPolicyList().addAll(policies);
			return policiesType;
		}
    	
    }
    
    private static final class PoliciesTypeJsonSerializer extends JsonSerializer<PoliciesType> {

		@Override
		public void serialize(PoliciesType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<Policy> policies = value.getPolicyList();
			if ((policies != null) && !policies.isEmpty()) {
				gen.writeStartArray();
				for (Policy policy : policies) {
					gen.writeObject(policy);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class LinksTypeJsonDeserializer extends JsonDeserializer<LinksType> {

		@Override
		public LinksType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<Link>> listTypeRef = new TypeReference<List<Link>>() {};
			List<Link> links = oc.readValue(p, listTypeRef);

			LinksType linksType = new LinksType();
			linksType.getLinkList().addAll(links);
			return linksType;
		}
    	
    }
    
    private static final class LinksTypeJsonSerializer extends JsonSerializer<LinksType> {

		@Override
		public void serialize(LinksType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<Link> links = value.getLinkList();
			if ((links != null) && !links.isEmpty()) {
				gen.writeStartArray();
				for (Link link : links) {
					gen.writeObject(link);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class FiltersTypeJsonDeserializer extends JsonDeserializer<FiltersType> {

		@Override
		public FiltersType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<Filter>> listTypeRef = new TypeReference<List<Filter>>() {};
			List<Filter> filters = oc.readValue(p, listTypeRef);

			FiltersType filtersType = new FiltersType();
			filtersType.getFilterList().addAll(filters);
			return filtersType;
		}
    	
    }
    
    private static final class FiltersTypeJsonSerializer extends JsonSerializer<FiltersType> {

		@Override
		public void serialize(FiltersType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<Filter> filters = value.getFilterList();
			if ((filters != null) && !filters.isEmpty()) {
				gen.writeStartArray();
				for (Filter filter : filters) {
					gen.writeObject(filter);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class V4AddressPoolsTypeJsonDeserializer extends JsonDeserializer<V4AddressPoolsType> {

		@Override
		public V4AddressPoolsType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<V4AddressPool>> listTypeRef = new TypeReference<List<V4AddressPool>>() {};
			List<V4AddressPool> pools = oc.readValue(p, listTypeRef);

			V4AddressPoolsType poolsType = new V4AddressPoolsType();
			poolsType.getPoolList().addAll(pools);
			return poolsType;
		}
    	
    }
    
    private static final class V4AddressPoolsTypeJsonSerializer extends JsonSerializer<V4AddressPoolsType> {

		@Override
		public void serialize(V4AddressPoolsType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<V4AddressPool> pools = value.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				gen.writeStartArray();
				for (V4AddressPool pool : pools) {
					gen.writeObject(pool);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class V6AddressPoolsTypeJsonDeserializer extends JsonDeserializer<V6AddressPoolsType> {

		@Override
		public V6AddressPoolsType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<V6AddressPool>> listTypeRef = new TypeReference<List<V6AddressPool>>() {};
			List<V6AddressPool> pools = oc.readValue(p, listTypeRef);

			V6AddressPoolsType poolsType = new V6AddressPoolsType();
			poolsType.getPoolList().addAll(pools);
			return poolsType;
		}
    	
    }
    
    private static final class V6AddressPoolsTypeJsonSerializer extends JsonSerializer<V6AddressPoolsType> {

		@Override
		public void serialize(V6AddressPoolsType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<V6AddressPool> pools = value.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				gen.writeStartArray();
				for (V6AddressPool pool : pools) {
					gen.writeObject(pool);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class V6PrefixPoolsTypeJsonDeserializer extends JsonDeserializer<V6PrefixPoolsType> {

		@Override
		public V6PrefixPoolsType deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			ObjectCodec oc = p.getCodec();

			TypeReference<List<V6PrefixPool>> listTypeRef = new TypeReference<List<V6PrefixPool>>() {};
			List<V6PrefixPool> pools = oc.readValue(p, listTypeRef);

			V6PrefixPoolsType poolsType = new V6PrefixPoolsType();
			poolsType.getPoolList().addAll(pools);
			return poolsType;
		}
    	
    }
    
    private static final class V6PrefixPoolsTypeJsonSerializer extends JsonSerializer<V6PrefixPoolsType> {

		@Override
		public void serialize(V6PrefixPoolsType value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			List<V6PrefixPool> pools = value.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				gen.writeStartArray();
				for (V6PrefixPool pool : pools) {
					gen.writeObject(pool);
				}
				gen.writeEndArray();
			}
		}
    	
    }
    
    private static final class ByteArrayJsonSerializer extends JsonSerializer<byte[]> {

		@Override
		public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeObject(Util.toHexString(value));
		}
    }
        
    private static final class ByteArrayJsonDeserializer extends JsonDeserializer<byte[]> {

		@Override
		public byte[] deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			String hex = p.getText();
			return Util.fromHexString(hex);
		}    	
    }

}
