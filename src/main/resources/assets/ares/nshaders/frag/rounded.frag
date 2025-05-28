#version 330 core

in vec4 vertColor;
in vec3 vertNorm;

out vec4 fragColor;

void main() {
    float alpha = 1.0;
    float offset = 1.0 - vertNorm.z;
    if(abs(vertNorm.x) > offset && abs(vertNorm.y) > offset) {
        float distance = length(abs(vertNorm.xy) - offset) - vertNorm.z;
        alpha = smoothstep(0.0, 1.0, 1 - (distance / 0.05f));
    }

    if(alpha == 0.0) discard;
    fragColor = vec4(vertColor.rgb, vertColor.a * alpha);
}
