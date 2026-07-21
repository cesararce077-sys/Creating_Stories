ServerEvents.recipes(event => {
  // End Remastered's vanilla recipes bypass the pack's boss-progression gate.
  event.remove({ id: 'endrem:exotic_eye' })
  event.remove({ id: 'endrem:undead_eye' })
  event.remove({ id: 'endrem:witch_eye' })
})
