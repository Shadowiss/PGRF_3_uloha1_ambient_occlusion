#version 420
in vec2 texCoord;

layout (location=0) out vec4 outColor0;

layout (binding=0) uniform sampler2D positionTexture;
layout (binding=1) uniform sampler2D normalTexture;
layout (binding=2) uniform sampler2D randomTexture;
uniform mat4 projection;

const float MAX_KERNEL_SIZE = 128;

void main() {
    vec3 position = texture(positionTexture, texCoord).xyz;
    vec3 normal = normalize(texture(normalTexture, texCoord).xyz);

    float AO = 0;
    float sampleRadius = 0.5;
    float bias = 0.01;
    float numSamples = 0;

    for (int i = 0; i < MAX_KERNEL_SIZE; i++) {
        float index = i * (1.0 / MAX_KERNEL_SIZE);

        vec3 sampleOffset = texture(randomTexture, vec2(index, 0)).rgb;

        vec4 samplePosition = vec4(position + sampleRadius * sampleOffset, 1);
        vec4 offset = projection * samplePosition;
        offset.xyz /= offset.w;
        offset.xy = offset.xy * 0.5 + vec2(0.5); // transformace do rozsahu <0;1>

        vec4 occluderPos = texture(positionTexture, offset.xy);

        AO += (occluderPos.z >= (samplePosition.z + bias) ? 1 : 0);
        numSamples++;
    }

    AO = 1.0 - clamp(AO / numSamples, 0, 1);

    outColor0 = vec4(vec3(AO), 1);
}
