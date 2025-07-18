const conductores = [{nombre:"Libardo", licencia:"cat c6", disponible: true,
experienciaAnios: 10}, {nombre:"Roberson", licencia:"cat c5", disponible: false,
    experienciaAnios: 5}, {nombre:"Johan", licencia:"cat c5", disponible: true,
    experienciaAnios: 11}]

const nombres = conductores.map(conductor => conductor.nombre)
console.log(nombres)
const conductoresFilter = conductores.filter(conductor => conductor.disponible
    && conductor.experienciaAnios >3)
console.log(conductoresFilter)

const especificConductor = conductores.find(conductor = conductor.nombre === "Libardo")
if (especificConductor !== undefined) {
    console.log(especificConductor)
} else {
    console.log("Conductor no encontrado")
}

const totalExperiencia = conductores.reduce((acumulador,conductor) => acumulador + conductor.experienciaAnios, 0)
console.log(totalExperiencia)

const factura = {id:"09188u", montoTotal : 150000, fecha: "2025-06-21", estaPagada: true,
    obtenerEstadoFactura: () => {

        if (estaPagada) {
            console.log("Pagada")
        } else {
            console.log("Pendiente de pago")
        }
}
}

factura.obtenerEstadoFactura(factura);