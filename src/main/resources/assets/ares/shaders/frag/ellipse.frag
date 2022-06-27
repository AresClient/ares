#version 330 core

in vec2 vertUV;
in vec4 vertColor;

out vec4 fragColor;

void main() {
    float distance = length(vertUV) * 0.5;
    float alpha = smoothstep(0.0, 1.5, (0.5 - distance) / fwidth(distance));
    if(alpha == 0.0) discard;
    fragColor = vec4(vertColor.rgb, vertColor.a * alpha);
}
