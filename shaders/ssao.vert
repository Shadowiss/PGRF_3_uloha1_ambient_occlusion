#version 420
in vec2 inPosition;

out vec2 texCoord;

void main() {
    gl_Position = vec4(inPosition, 0, 1); // quad in <-1;1>
    texCoord = inPosition * 0.5 + 0.5;
}
