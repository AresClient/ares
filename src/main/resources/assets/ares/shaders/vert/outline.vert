#version 330 core

layout (location = 0) in vec3 pos;

out vec2 texCoord;

void main() {
    vec2 screenPos = pos.xy * 2.0 - 1.0;
    gl_Position = vec4(screenPos.x, screenPos.y, 1.0, 1.0);
    texCoord = pos.xy;
}
