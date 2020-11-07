#version 150

in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

out vec2 texCoord;

void main() {
    texCoord = inPosition;
    vec2 position = inPosition * 2 - 1;
    gl_Position = projection * view * vec4(position, 0.0, 1.0);
}
